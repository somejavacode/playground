import ufw.FixDateFormat;
import ufw.Hex;
import ufw.Log;
import ufw.Timer;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
        private long date;

        /**
         * @param operation operation type
         * @param fileInfo updated/new info for UPD, CRE. old info for DEL.
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
        }

        /** encode to line, separator is "colon" */
        public String toLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(date);
            sb.append(SEPARATOR);
            sb.append(operation); // will be string like "CRE"
            sb.append(SEPARATOR);
            sb.append(fileInfo.toLine());
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

        public String toString() {
            return FixDateFormat.formatSync(date) + " - " + toLine();
        }
    }

    /**
     * data representing file metadata.<br/>
     * change date was omitted
     */
    private static class FileInfo {
        private String path;
        private long size;
        private byte[] hash;

        public FileInfo(String path, long size, byte[] hash) {
            Validate.isTrue(!path.contains(SEPARATOR), "file path contains " + SEPARATOR +
                    " such files are currently not supported. " + path);
            this.path = path;
            this.size = size;
            this.hash = hash;
        }

        public FileInfo(StringTokenizer st) {
            path = st.nextToken();
            size = Long.parseLong(st.nextToken());
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

        public void setHash(byte[] hash) {
            this.hash = hash;
        }

        public String toLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(path);
            sb.append(SEPARATOR);
            sb.append(size);
            sb.append(SEPARATOR);
            sb.append(Hex.toString(hash));
            return sb.toString();
        }

        public String toString() {
            return toLine();
        }
    }

    public static void main(String[] args) throws Exception {

        boolean logTimer = false;  // enable to check performance for large files or sets
        Timer t = new Timer("main", "read index", logTimer);
        if (args.length == 0) {
            System.out.println("TODO: show usage");
            // print usage
            return;
        }

        String baseDir = ".";  // current directory
        if (args.length > 1) {
            baseDir = args[1];
        }

        String command = args[0].toLowerCase();
        ArrayList<FileChange> index = null;
        HashMap<String, FileInfo> merged = null;

        File infoFile = new File(baseDir + SEP + SCAN_INDEX);
        if (infoFile.exists()) {
            // read index and create merged file list
            index = readIndex(infoFile);
            validateDateOrder(index);
            merged = mergeIndex(index);
            t.split("scan", index.size() + " files", logTimer);
        }
        else {
            t.split("scan", "no index found", logTimer);
        }

        if (command.startsWith("l")) { // list index
            if (index == null) {
                Log.warn(SCAN_INDEX + " not found");
                return;
            }
            for (FileChange fc : index) {
                System.out.println(fc);
            }
            return;
        }

        if (command.startsWith("m")) { // list merged files
            if (merged == null) {
                Log.warn(SCAN_INDEX + " not found");
                return;
            }
            for (FileInfo fi : merged.values()) {  // todo sorting?
                System.out.println(fi.toLine());
            }
            return;
        }
        if (command.startsWith("h")) { // summary hash
            if (merged == null) {
                Log.warn(SCAN_INDEX + " not found");
                return;
            }
            Log.info("summary hash: " + Hex.toString(summaryHash(merged)));
            return;
        }

        if (command.startsWith("u") || command.startsWith("s")) { // update index (add changes), status (list changes)

            boolean update = command.startsWith("u");
            // fast: no hash, only file size "heuristics"
            boolean fast = command.length() > 1 && command.charAt(1) == 'f';

            // scan current files...
            // note: during scan files shall not be changed or locked
            ArrayList<FileInfo> files = scanFiles(new File(baseDir), fast);
            long scanTime = System.currentTimeMillis(); // use end of scan.
            t.split("sort", files.size() + " files", logTimer); // start sort, show files read

            // list of detected changes
            ArrayList<FileChange> changes = new ArrayList<>();

            if (merged == null) {
                // for update need to scan with hash calculation
                if (fast && update) {
                    files = scanFiles(new File(baseDir), false);
                }
                // fresh index, all entries are "CRE"
                for (FileInfo fi : files) {
                    changes.add(new FileChange(FileChange.Operation.CRE, fi, scanTime));
                }
            }
            else {
                // sequence of states fore each file CRE UPD* DEL  (then starts again with CRE)

                // go through list of current files, "consume" files from index
                for (FileInfo fi : files) {
                    FileInfo match = merged.get(fi.getPath());
                    if (match == null) {
                        checkHash(baseDir, fi, update);
                        changes.add(new FileChange(FileChange.Operation.CRE, fi, scanTime));
                    }
                    else {
                        // found with path
                        // "fast" detect with size, this will fail to detect changed file with same size
                        if (fast) {
                            if (fi.getSize() != match.getSize()) {
                                // different size -> update
                                checkHash(baseDir, fi, update);
                                changes.add(new FileChange(FileChange.Operation.UPD, fi, scanTime));
                            }
                        }
                        else if (!Arrays.equals(fi.getHash(), match.getHash())) {
                            // different hash -> update
                            changes.add(new FileChange(FileChange.Operation.UPD, fi, scanTime));
                        }

                        // "else" same hash (same size), nothing to do.
                        Validate.isTrue(merged.remove(match.getPath()) != null, "failed to remove from map: " + match.toString());
                    }
                }
                // remaining entries have been deleted
                for (FileInfo fi : merged.values()) { // todo:sorting?
                    changes.add(new FileChange(FileChange.Operation.DEL, fi, scanTime));
                }
            }
            if (update && changes.size() > 0) {
                // update: write changes to index
                writeAppendIndex(infoFile, changes);
            }
            else {
                // status: list changes
                for (FileChange fc : changes) {
                    System.out.println(fc);
                }
            }
            t.stop(logTimer);
        }
        else {
            throw new Exception("Unknown command: " + command);
        }
    }

    private static ArrayList<FileChange> readIndex(File file) throws Exception {
        ArrayList<FileChange> changes = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET));
        String line;
        while ((line = br.readLine()) != null) {
            changes.add(new FileChange(line));
        }
        br.close();
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

    /**
     * scan all files recursively, calculate hashes
     */
    private static ArrayList<FileInfo> scanFiles(File baseDir, boolean fast) throws Exception {
        ArrayList<FileInfo> files = new ArrayList<>();
        scanFilesRecursive(baseDir, baseDir, files, fast);
        return files;
    }

    private static void scanFilesRecursive(File baseDir, File dir, ArrayList<FileInfo> files, boolean fast) throws Exception {
        Validate.isTrue(dir.exists(), "not found: " + dir);
        File[] fileArray = dir.listFiles();
        // Validate.isTrue(dir.isDirectory(), "not a directory: " + dir);
        Validate.isTrue(fileArray != null, "not a directory: " + dir); // null if not a directory
        for (File f : fileArray) {
            //  skip ".*" files/dirs
            if (f.getName().startsWith(".")) {  // todo: more flexible (".scan-ignore")
                continue;
            }
            if (f.isDirectory()) {
                // recursive...
                scanFilesRecursive(baseDir, f, files, fast);
            }
            else {
                byte[] hash = fast ? null : getHash(f);
                files.add(new FileInfo(cleanPath(baseDir, f.getPath()), f.length(), hash));
            }
        }
    }

    /**
     * add hash if needed but missing
     */
    private static void checkHash(String baseDir, FileInfo fi, boolean needHash) throws Exception {
        if (needHash && fi.getHash() == null) {
            fi.setHash(getHash(new File(baseDir + SEP + fi.getPath())));
        }
    }

    /**
     * clean up path: remove prefix, use unix path separator
     */
    private static String cleanPath(File dir, String path) {
        String prefix = fixSeparator(dir.getPath());
        String prefixFull = fixSeparator(dir.getAbsolutePath());  // may contain things like "C:"
        String pathToClean = fixSeparator(path);
        String clean1 = stripPrefix(prefixFull, pathToClean);
        String clean2 = stripPrefix(prefix, clean1);
        return stripPrefix("/", clean2);
    }

    private static String stripPrefix(String prefix, String value) {
        if (value.startsWith(prefix)) {
            return value.substring(prefix.length());
        }
        return value;
    }

    private static String fixSeparator(String path) {
        if (SEP.equals("\\")) {
            return path.replace("\\", "/");
        }
        return path;
    }


    /**
     * check that each date in change list is equal or bigger ("later") that previous entry
     */
    private static void validateDateOrder(ArrayList<FileChange> changes) {
        long date = 0;
        for (FileChange fc : changes) {
            if (fc.getDate() >= date) {
                date = fc.getDate();
            }
            else {
                throw new RuntimeException("got date problem.");
            }
        }
    }

    /**
     * get hash of file
     */
    private static byte[] getHash(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] buffer = new byte[16384];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            md.update(buffer, 0, bytes);
        }
        fis.close();
        return md.digest();
    }

    /**
     * create hash that represents set of all files.
     * Note: files are sorted by path to create same hash for same set of files.
     */
    private static byte[] summaryHash(HashMap<String, FileInfo> files2) throws Exception {
        ArrayList<FileInfo> files = new ArrayList<>();  // todo: rework?
        files.addAll(files2.values());
        sortByPath(files);
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        for (FileInfo fi : files) {
            md.update(fi.getPath().getBytes(CHARSET));
            md.update(fi.getHash());
        }
        return md.digest();
    }

    private static void sortByPath(ArrayList<FileInfo> files) {
        Collections.sort(files, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
    }

    /**
     * get result list of files by merging index
     */
    private static HashMap<String, FileInfo> mergeIndex(ArrayList<FileChange> index) {

        HashMap<String, FileInfo> expected = new HashMap<>();  // track FileInfo per path

        // go through change list
        for (FileChange fc : index) {
            FileInfo fi = fc.getFileInfo();
            String path = fi.getPath();
            FileInfo fiNew = new FileInfo(path, fi.getSize(), fi.getHash());
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
                case DEL:
                    Validate.isTrue(expected.remove(path) != null);
                    break;
                default:
                    throw new RuntimeException("unknown operation " + fc.getOperation());
            }
        }
        return expected;
    }
}
