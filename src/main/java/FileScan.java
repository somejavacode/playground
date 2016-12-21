import ufw.FixDateFormat;
import ufw.Hex;
import ufw.Validate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;

public class FileScan {

    private static final String SEP = File.separator;
    private static final String SCAN_INDEX = ".scan-index";
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String SEPARATOR = ":";
    private static final String HASH_ALGORITHM = "SHA-256";

    private static class FileChange {
        enum Operation {
            /** create */
            CRE,
            /** update */
            UPD,
            /** move */
            MOV,
            /** delete */
            DEL;

            public static Operation getByName(String name) {
                for (Operation o : Operation.values()) {
                    if (o.name().equals(name)) {
                        return o;
                    }
                }
                throw new RuntimeException("unknown name: " + name);
            }
        }

        private Operation operation;
        private FileInfo fileInfo;
        private String oldPath;  // for operation MOV
        private long date;

        /**
         * @param operation operation type
         * @param fileInfo updated/new info for UPD, MOV, CRE. old info for DEL.
         * @param date operation date
         */
        public FileChange(Operation operation, FileInfo fileInfo, long date) {
            this.operation = operation;
            this.fileInfo = fileInfo;
            this.date = date;
        }

        /** parse from line */
        public FileChange(String line) {
            StringTokenizer st = new StringTokenizer(line, SEPARATOR);
            date = Long.parseLong(st.nextToken());
            operation = Operation.getByName(st.nextToken());
            fileInfo = new FileInfo(st);
            if (operation == Operation.MOV) {
                oldPath = st.nextToken();
            }
        }

        /** encode to line, separator is "colon" */
        public String toLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(date);
            sb.append(SEPARATOR);
            sb.append(operation); // will be string like "CRE"
            sb.append(SEPARATOR);
            sb.append(fileInfo.toLine());
            if (operation == Operation.MOV) {
                sb.append(SEPARATOR);
                sb.append(oldPath);
            }
            return sb.toString();
        }

        public Operation getOperation() {
            return operation;
        }

        public FileInfo getFileInfo() {
            return fileInfo;
        }

        public long getDate() {
            return date;
        }

        public String getOldPath() {
            return oldPath;
        }

        public void setOldPath(String oldPath) {
            this.oldPath = oldPath;
        }
    }

    /** data representing file metadata */
    private static class FileInfo {
        private String path;
        private long size;
        private long modified;
//        private long created;  later?
        private byte[] hash;

        public FileInfo(String path, long size, long modified, byte[] hash) {
            Validate.isTrue(!path.contains(SEPARATOR), "file path contains " + SEPARATOR +
                            " such files are currently not supported. " + path);
            this.path = path;
            this.modified = modified;
            this.size = size;
            this.hash = hash;
        }

        public FileInfo(StringTokenizer st) {
            path = st.nextToken();
            size = Long.parseLong(st.nextToken());
            // modified is no needed
            String hashString = st.nextToken();
            // rather dirty..
            hash = hashString.equals(Hex.NULL_ARRAY) ? null : Hex.fromString(hashString);
        }

        public String getPath() {
            return path;
        }

        public long getSize() {
            return size;
        }

        public byte[] getHash() {
            return hash;
        }

        public long getModified() {
            return modified;
        }

        public String toLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(path);
            sb.append(SEPARATOR);
            sb.append(size);
            sb.append(SEPARATOR);
            // modified is no needed
            sb.append(Hex.toString(hash));
            return sb.toString();
        }

        public String toString() {
            return toLine() + SEPARATOR + FixDateFormat.formatSync(modified);
        }
    }

    public static void main(String[] args) throws Exception {

//        args = new String[] {"u"}; // testing
//         args = new String[] {"l"}; // testing

        if (args.length == 0) {
            System.out.println("TODO: show usage");
            // print usage
            return;
        }


        String command = args[0].toLowerCase();
        ArrayList<FileChange> index = null;
        ArrayList<FileInfo> merged = null;
        long lastIndex = 0;

        String baseDir = ".";  // current directory
        File infoFile = new File(baseDir + SEP + SCAN_INDEX);
        if (infoFile.exists()) {
            // read index and create merged file list
            index = readIndex(infoFile);
            validateDateOrder(index);
            lastIndex = index.get(index.size() - 1).getDate(); // date of last index entry
            merged = mergeIndex(index);
        }

        if (command.startsWith("l")) { // list index
            if (index == null) {
                System.out.println(SCAN_INDEX + "not found");
                return;
            }
            for (FileChange fc : index) {
                System.out.println(fc.toLine());
            }
        }

        if (command.startsWith("m")) { // list merged files
            if (merged == null) {
                System.out.println(SCAN_INDEX + "not found");
                return;
            }
            for (FileInfo fi : merged) {
                System.out.println(fi.toLine());
            }
        }

//        else if (command.startsWith("v")) { // validate all hashes  "status" will validate hashes.
//        }
        else if (command.startsWith("u") || command.startsWith("s")) { // update index (add changes), status (list changes)

            // current files...
            long scanTime = System.currentTimeMillis(); // start of scan
            ArrayList<FileInfo> files = scanFiles(new File(baseDir), true);  // hash all
            ArrayList<FileChange> changes = new ArrayList<>();

            if (merged == null) {
                // fresh index, all entries are "CRE"
                for (FileInfo fi : files) {
                    changes.add(new FileChange(FileChange.Operation.CRE, fi, fi.getModified()));
                }
            }
            else {
                // file is  in files  in index
                // CRE         Y         N     calculate hash, check for MOV ...
                // DEL         N         Y
                // UPD         Y         Y     date is younger as in index, calculate hash
                // ERROR       Y         Y     date is older than in index
                // OK          Y         Y     date is same as in index, option validate hash

                // sequence of states fore each file CRE [UPD|MOV*] [DEL]  (then start again with CRE)

                // go through file list (files older than lastIndex), "consume" expected files (case OK, ERROR)
                for (FileInfo fi : files) {
                    if (fi.getModified() <= lastIndex) { // OK, ERROR, missing
                        FileInfo match = findMatch(fi, merged, true);
                        if (match == null) {
                            continue;
                        }
                        if (fi.getModified() == match.getModified() &&
                                fi.getSize() == match.getSize() &&
                                Arrays.equals(fi.getHash(), match.getHash())) {
                            Validate.isTrue(merged.remove(match), "failed to remove: " + match.toString());
                        }
                        else {
                            throw new RuntimeException("got invalid modification. file=" + fi.toString());
                        }
                    }
                    else { // create, update, move
                        FileInfo match = findMatch(fi, merged, false);
                        if (match == null) {
                            // todo: check for move... currently: DEL/CRE
                            changes.add(new FileChange(FileChange.Operation.CRE, fi, fi.getModified()));
                        }
                        else {
                            // further checks?
                            changes.add(new FileChange(FileChange.Operation.UPD, fi, fi.getModified()));
                            Validate.isTrue(merged.remove(match), "failed to remove: " + match.toString());
                        }
                    }
                }
                // remaining entries...
                for (FileInfo fi : merged) {
                    // delete time is unknown, use date of "found missing".
                    changes.add(new FileChange(FileChange.Operation.DEL, fi, scanTime));
                }

                //  remaining list of  expected  files: missing files (DEL or MOV)

                //  continue date younger than lastIndex .. case UPD, case CRE (check for MOV)
                // missing and not MOV : DEL
            }
            if (command.startsWith("u")) {  // remove c&w code!
                // update: write changes to index
                writeAppendIndex(infoFile, changes);
            }
            else {
                // status: list changes
                for (FileChange fc : changes) {
                    System.out.println(fc.toLine());
                }
            }
        }

    }

    private static FileInfo findMatch(FileInfo file, ArrayList<FileInfo> changes, boolean limit) {
        for (FileInfo fi : changes) {
            if (fi.getPath().equals(file.getPath())) {
                return fi;
            }
            if (limit && fi.getModified() > file.getModified()) {
                return null;
            }
        }
        return null;
    }

    private static ArrayList<FileChange> readIndex(File file) throws Exception {
        ArrayList<FileChange> changes = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET));
        String line;
        while ((line = br.readLine()) != null) {
            changes.add(new FileChange(line));
        }
        return changes;
    }

    private static void writeAppendIndex(File file, ArrayList<FileChange> changes) throws Exception {
        // this nice cascade is required to control: "append" and "charset".
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), CHARSET));
        for (FileChange fc : changes) {
            writer.write(fc.toLine());
            writer.write("\n"); // unix line break 0x0A
        }
        writer.close();
    }

    // scan all files recursively, optional calculate hash, result sorted by change date.
    private static ArrayList<FileInfo> scanFiles(File dir, boolean calculateHash) throws Exception {
        ArrayList<FileInfo> files = new ArrayList<>();
        scanFilesRecursive(dir, files, calculateHash);
        sortByDate(files);
        return files;
    }

    // scan directory recursively
    private static void scanFilesRecursive(File dir, ArrayList<FileInfo> files, boolean calculateHash) throws Exception {
        Validate.isTrue(dir.exists(), "not found: " + dir);
        File[] fileArray = dir.listFiles();
        // Validate.isTrue(dir.isDirectory(), "not a directory: " + dir);
        Validate.isTrue(fileArray != null, "not a directory: " + dir); // null if not a directory
        for (File f : fileArray) {
            //  skip ".*" files/dirs
            if (f.getName().startsWith(".")) {  // todo: more generic
                continue;
            }

            if (f.isDirectory()) {
                // recursive...
                scanFilesRecursive(f, files, calculateHash);
            }
            else {
                byte[] hashValue = null;
                if (calculateHash) {
                    hashValue = getHash(f);
                }
                files.add(new FileInfo(f.getPath(), f.length(), f.lastModified(), hashValue));
            }
        }
    }

    private static void sortByDate(ArrayList<FileInfo> files) {
        Collections.sort(files, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                return Long.compare(o1.getModified(), o2.getModified());
            }
        });
    }

    // check that each date in change list is equal or bigger ("later") that previous entry
    private static void validateDateOrder(ArrayList<FileChange> changes) {
        long date = 0;
        for (FileChange fc : changes) {
            if (fc.getDate() >= date) {
                date = fc.getDate();
            }
            else {
                throw new RuntimeException("got date problem");
            }
        }
    }

    private static byte[] getHash(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] buffer = new byte[16384];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            md.update(buffer, 0, bytes);
        }
        return md.digest();
    }

    // get result list of files based on "merged" index
    private static ArrayList<FileInfo> mergeIndex(ArrayList<FileChange> index) {

        HashMap<String, FileInfo> expected = new HashMap<>();  // collect FileInfo per path

        // go through change list
        for (FileChange fc : index) {
            FileInfo fi = fc.getFileInfo();
            String path = fi.getPath();
            // create FileInfo with date of FileChange
            FileInfo fiNew = new FileInfo(path, fi.getSize(), fc.getDate(), fi.getHash());
            switch (fc.getOperation()) {
                case CRE:
                    // add info, validate that entry is new
                    Validate.isTrue(expected.put(path, fiNew) == null);
                    break;
                case UPD:
                    FileInfo old = expected.put(path, fiNew);
                    Validate.isTrue(old != null);
                    // more checks?
                    break;
                case MOV:
                    FileInfo old2 = expected.remove(fc.getOldPath());
                    Validate.isTrue(old2 != null);
                    Validate.isTrue(expected.put(path, fiNew) == null);
                    // more checks?
                    break;
                case DEL:
                    Validate.isTrue(expected.remove(path) != null);
                    break;
                default:
                    throw new RuntimeException("unknown operation " + fc.getOperation());
            }
        }
        ArrayList<FileInfo> files = new ArrayList<>();
        files.addAll(expected.values());
        sortByDate(files);
        return files;
    }
 }
