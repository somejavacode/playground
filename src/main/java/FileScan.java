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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

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

        args = new String[] {"u"}; // testing
//        args = new String[] {"l"}; // testing

        if (args.length == 0) {
            // print usage
            return;
        }

        String command = args[0].toLowerCase();
        ArrayList<FileChange> index = null;
        ArrayList<FileChange> changes = new ArrayList<FileChange>();

        String baseDir = ".";  // current directory...
        File infoFile = new File(baseDir + SEP + SCAN_INDEX);
        if (infoFile.exists()) {
            index = readChanges(infoFile);
            validateDateOrder(index);
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
        else if (command.startsWith("s")) { // status: list changes
            // scanFiles(new File(baseDir), changes, index);
            for (FileChange fc : changes) {
                System.out.println(fc);
            }
        }
        else if (command.startsWith("v")) { // validate all hashes

        }
        else if (command.startsWith("u")) { // update index
            ArrayList<FileInfo> files = new ArrayList<FileInfo>();
            scanFiles(new File(baseDir), files, true);
            sortByDate(files);
//            for (FileInfo f : files) {
//                System.out.println(f);
//            }

            // hack, no "diff", all create...
            for (FileInfo fi : files) {
                changes.add(new FileChange(FileChange.Operation.CRE, fi, fi.getModified()));
            }
            writeAppendChanges(infoFile, changes);
        }

    }

    private static ArrayList<FileChange> readChanges(File file) throws Exception {
        ArrayList<FileChange> changes = new ArrayList<FileChange>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET));
        String line;
        while ((line = br.readLine()) != null) {
            changes.add(new FileChange(line));
        }
        return changes;
    }

    private static void writeAppendChanges(File file, ArrayList<FileChange> changes) throws Exception {
        // this nice cascade is required to control: "append" and "charset".
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), CHARSET));
        for (FileChange fc : changes) {
            writer.write(fc.toLine());
            writer.write("\n"); // unix line break 0x0A
        }
        writer.close();
    }

    // scan directory recursively, collect changes for all files
    private static void scanFiles(File dir, ArrayList<FileInfo> files, boolean calculateHash) throws Exception {
        Validate.isTrue(dir.exists(), "not found: " + dir);
        Validate.isTrue(dir.isDirectory(), "not a directory: " + dir);
        for (File f : dir.listFiles()) {
            //  skip ".*" files/dirs
            if (f.getName().startsWith(".")) {  // todo: more generic
                continue;
            }

            if (f.isDirectory()) {
                // recursive...
                scanFiles(f, files, calculateHash);
            }
            else {
                byte[] hashValue = null;
                if (calculateHash) {
                    hashValue = getHash(f);
                }
                files.add(new FileInfo(f.getPath(), (int) f.length(), f.lastModified(), hashValue));
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
 }
