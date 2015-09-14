import ufw.FileTool;
import ufw.Hex;
import ufw.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * a simplistic (and 100% python free) implementation of a pak file handling
 *
 * https://groups.google.com/a/chromium.org/forum/?fromgroups=#!topic/chromium-dev/agGjTt4Dmcw
 */
public class PakPacker {

    public static void main(String[] args) throws Exception {

// testing variants, too lazy to make this a unit test but "round trip" was OK (result was identical) finally...
//        args = new String[] {"t", "f:/tmp/test.pak"};
//        args = new String[] {"x", "f:/tmp/test.pak", "f:/tmp/testpak"};  // extract to directory
//        args = new String[] {"c", "f:/tmp/testc.pak", "f:/tmp/testpak"}; // create from directory

        if (args.length < 2) {
            // tar like syntax. command txc, source of destination file, source or destination directory
            Log.info("Syntax: PakPacker t|x|c <pakfile> [<directory>]");
            return;
        }

        String command = args[0].toLowerCase();
        String pakFileName = args[1];
        String directory = null;
        if (args.length > 2) {
            directory = args[2];
        }
        if (command.equals("c")) {
            File srcDir = new File(directory);
            FileTool.testProperties(srcDir, true, false, false, true, true, false);

            File pakFile = new File(pakFileName);
            FileTool.testProperties(pakFile, false, true, false, false, false, false);
            FileTool.createOK(pakFile);

            FileOutputStream fos = new FileOutputStream(pakFile);

            // find all files....
            File[] files = srcDir.listFiles();
            HashMap<Integer, File> fileMap = new HashMap<Integer, File>(files.length);
            for (File file : files) {
                int id = 0;
                try {
                    id = Integer.parseInt(file.getName());
                }
                catch (NumberFormatException nfe) {
                    throw new RuntimeException("found file with non-numeric name: " + file.getName());
                }
                fileMap.put(id, file);
            }
            Log.info("number of resources: " + files.length);

            // now sort files per id...
            ArrayList<Integer> resIds = new ArrayList<Integer>(fileMap.keySet());
            Collections.sort(resIds);
            ArrayList<Resource> resources = new ArrayList<Resource>();

            // prepare resource headers
            for (Integer resId : resIds) {
                Resource res = new Resource();
                if (resId > Short.MAX_VALUE) {
                    throw new RuntimeException("resourceId too high: " + resId);
                }
                res.id = (short) resId.intValue();
                res.size = (int) fileMap.get(resId).length();
                resources.add(res);
            }
            // add "final" resource
            Resource finalRes = new Resource();
            finalRes.id = 0;
            resources.add(finalRes);

            // calculate offsets
            int resOffset = 9 + 6 * (resIds.size() + 1);  // first offset value
            for (Resource res : resources) {
                res.offset = resOffset;
                resOffset += res.size;
                Log.info(res.toString());
            }

            // write header
            fos.write(intToHex(4));  // version
            fos.write(intToHex(files.length));  // count
            fos.write(new byte[]{1});  // encoding = 1

            // write resource headers
            for (Resource res : resources) {
                fos.write(shortToHex(res.id));
                fos.write(intToHex(res.offset));
            }

            // write resource content
            for (Integer resId : resIds) {
                File resFile = fileMap.get(resId);
                FileTool.writeFileToStream(resFile, fos);
            }
            fos.close();
        }

        if (command.equals("x") || command.equals("t")) {

            boolean extract = command.equals("x");

            File destDir = null;

            if (extract) {
                destDir = new File(directory);
                FileTool.testProperties(destDir, false, true, false, false, false, false);
                FileTool.mkDirsOK(destDir);
            }

            File pakFile = new File(pakFileName);
            FileTool.testProperties(pakFile, true, false, true, false, true, false);

            RandomAccessFile pakRaf = new RandomAccessFile(pakFile, "r");

            // dump raw header
            // byte[] header = new byte[128];
            // pakRaf.read(header);
            // log.log("header\n" + Hex.toStringBlock(header));

            // read pak header
            PakFile pf = getPakHeader(pakRaf);
            Log.info("reading PAK header: " + pf);

            ArrayList<Resource> resources = new ArrayList<Resource>(pf.resourceCount + 1);

            // read resource headers
            for (int i = 0; i <= pf.resourceCount; i++) {
                Resource res = getResourceHeader(pakRaf);
                resources.add(res);
            }

            // check file size with "last" resource
            long fileSize = pakRaf.length();
            long resFileSize = resources.get(pf.resourceCount).offset;
            if (fileSize != resFileSize) {
                throw new RuntimeException("problem with final resource entry");
            }

            for (int i = 0; i < pf.resourceCount; i++) {
                Resource res = resources.get(i);
                Resource nextRes = resources.get(i + 1);
                int size = nextRes.offset - res.offset;
                if (size < 0) {
                    throw new RuntimeException("invalid resource size. this=" + res +  " next=" + nextRes);
                }
                res.size = size;
                Log.info(res.toString());
                if (extract) {
                    File resFile = new File(directory + File.separator + res.id);
                    if (resFile.exists()) {
                        throw new RuntimeException("file exists: " + resFile);
                    }
                    FileTool.testProperties(resFile, false, true, false, false, false, false);
                    FileTool.createOK(resFile);
                    FileTool.readFileFromRAF(pakRaf, res.offset, resFile, res.size);
                }
                else {
                    // show initial 64 bytes...
                    int maxShow = 64;
                    int showBytes = size > maxShow ? maxShow : size;

                    pakRaf.seek(res.offset);
                    byte[] content = new byte[showBytes];
                    pakRaf.read(content);
                    Log.info("content preview (first 64 bytes):\n" + Hex.toStringBlock(content));
                }
            }
        }
    }

    private static PakFile getPakHeader(RandomAccessFile pakRaf) throws IOException {
        PakFile pf = new PakFile();
        byte[] intBytes = new byte[4];
        pakRaf.seek(0);
        pakRaf.read(intBytes);
        pf.version = hexToInt(intBytes);
        pakRaf.read(intBytes);
        pf.resourceCount = hexToInt(intBytes);
        pf.encoding = pakRaf.readByte();
        return pf;
    }

    private static Resource getResourceHeader(RandomAccessFile pakRaf) throws IOException {
        byte[] intBytes = new byte[4];
        byte[] shortBytes = new byte[2];
        Resource res = new Resource();
        pakRaf.read(shortBytes);
        res.id = hexToShort(shortBytes);
        pakRaf.read(intBytes);
        res.offset = hexToInt(intBytes);
        return res;
    }

    private static byte[] intToHex(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value & 0xFF);
        bytes[1] = (byte) (value >> 8 & 0xFF);
        bytes[2] = (byte) (value >> 16 & 0xFF);
        bytes[3] = (byte) (value >> 24 & 0xFF);
        return bytes;
    }

    private static byte[] shortToHex(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xFF);
        bytes[1] = (byte) (value >> 8 & 0xFF);
        return bytes;
    }

    private static int hexToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new RuntimeException("invalid byte count for int" + bytes.length);
        }
        int ch1 = bytes[3] & 0xFF;  // note: 0xFF fixes sign.
        int ch2 = bytes[2] & 0xFF;
        int ch3 = bytes[1] & 0xFF;
        int ch4 = bytes[0] & 0xFF;
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
    }

    private static short hexToShort(byte[] bytes) {
        if (bytes.length != 2) {
            throw new RuntimeException("invalid byte count for short " + bytes.length);
        }
        int ch3 = bytes[1] & 0xFF;
        int ch4 = bytes[0] & 0xFF;
        return (short) ((ch3 << 8) + ch4);
    }

    private static class PakFile {
        private int version;
        private int resourceCount;
        private byte encoding;

        @Override
        public String toString() {
            return "PakFile{" +
                    "version=" + version +
                    ", resourceCount=" + resourceCount +
                    ", encoding=" + encoding +
                    '}';
        }
    }

    private static class Resource {
        private short id;
        private int size;
        private int offset;

        @Override
        public String toString() {
            return "Resource{" +
                    "id=" + id +
                    ", size=" + size +
                    ", offset=" + offset +
                    '}';
        }
    }
}
