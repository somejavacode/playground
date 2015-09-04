import ufw.Log;
import ufw.Timer;
import ufw.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * HTTP: old RFC2616 -> obsoleted by RFC7230..RFC7235 2014-06
 *  http://tools.ietf.org/html/rfc2616 176 pages
 *  http://tools.ietf.org/html/rfc7230 .. 35
 *   1.  "Message Syntax and Routing" (this document) 89 pages
 *   2.  "Semantics and Content" [RFC7231] 101 pages
     3.  "Conditional Requests" [RFC7232] 28 pages
     4.  "Range Requests" [RFC7233] 25 pages
     5.  "Caching" [RFC7234] 43 pages
     6.  "Authentication" [RFC7235] 19 pages
 */

public class HttpRequester {

    private static final String ASCII = "ASCII";

    public static void main(String[] args) throws Exception {

        args = new String[] {"http://localhost:8080/test", "10"};

        UrlParts parts = new UrlParts(args[0]);

        String host = parts.host;

        // String host = "www.orf.at";   // java.net.ConnectException: Connection timed out: connect
        int port = parts.port;  // hope there is no response...
        int connectTimeout = 5000;
        int socketTimeout = 1000;

         // Socket s  = new Socket(host, port); // NOTE: too easy, does implicit "connect". cannot control connection timeout. tested approx 20s
        // the hard way
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        Socket s = new Socket();
        Log.info("start connect host=" + host + " port=" + port);
        s.connect(socketAddress, connectTimeout); // this "hard way" one can set the timeout....

        s.setSoTimeout(socketTimeout);
        // s.setReceiveBufferSize(1024);
        OutputStream os = s.getOutputStream();
        InputStream is = s.getInputStream();
        int seed = 13;
        int dlSize = 1000000;
        int dlThrottle = 5; // milliseconds every 8k

        // RequestHeader r = new RequestHeader("/testapp/test?echo=33", "GET");
        // RequestHeader r = new RequestHeader("/testapp/logout.png", "GET");
        // RequestHeader r = new RequestHeader("/testapp/test?d=128&r=1", "GET"); // 33 random bytes
        RequestHeader r = new RequestHeader("/testapp/test?d=" +dlSize + "&r=" + seed + "&t=" + dlThrottle, "GET");
        r.addHeader("Host", host);
//        r.addHeader("User-Agent", "Mickey Mouse");

        byte[] reqBytes = r.getBytes(); // this can be reused for certain benchmarks

        // Log.info("req bytes1:\n" + Hex.toStringBlock(reqBytes));
        Timer t = new Timer("download", true);
        os.write(reqBytes);
//        os.write(reqBytes);
//        os.write(reqBytes);  // test 3x pipeline
        os.flush();
        t.split("request header flush");

        // interesting: for 3x pipeline: first read one, second read two responses.

        byte[] body;

        ResponseHeader res = new ResponseHeader();
        res.fromInputStream(is);
        t.split("response header received " + res.statusCode);

        body = readResponse(is, res.contentLength, true, res.chunked, seed);
        // Log.info("body:\n" + Hex.toStringBlock(body));
        t.split("response body received");

        int ulSize = 1000000;
        int ulThrottle = 5;
        // boolean chunked = ulSize > 32768;
        boolean chunked = true;
        int blockSize = 4096;
        int clientThrottle = 0;
        r = new RequestHeader("/testapp/test?u=" + ulSize + "&r=" + seed + "&t=" + ulThrottle, "POST");
        r.addHeader("Host", host);
        r.addHeader("Content-Type", "application/octet-stream");
        if (chunked) {
            r.addHeader("Transfer-Encoding", "chunked");
        }
        else {
            r.addHeader("Content-Length", ulSize + "");
        }

        reqBytes = r.getBytes();
        // Log.info("req bytes2:\n" + Hex.toStringBlock(reqBytes));

        t = new Timer("upload", true);
        os.write(reqBytes); // request header
        t.split("request header write");
        writeRequest(os, null, ulSize, chunked, seed, blockSize, clientThrottle);
        t.split("request body written");
        os.flush();

        res = new ResponseHeader();
        res.fromInputStream(is);
        t.split("response header received " + res.statusCode);
        body = readResponse(is, res.contentLength, true, res.chunked, 0);
        // Log.info("body:\n" + Hex.toStringBlock(body)); NPE
        /*
        res = new ResponseHeader();
        res.fromInputStream(is);
        body = readResponse(is, res.contentLength, false, res.chunked, seed);
        Log.info("body:\n" + Hex.toStringBlock(body));

        res = new ResponseHeader();
        res.fromInputStream(is);
        body = readResponse(is, res.contentLength, false, res.chunked, seed);
        Log.info("body:\n" + Hex.toStringBlock(body));
        */

//        while ((bytesRec = is.read(buffer)) > 0) {  // how to "express": got 500bytes and there is "no more".
//            Log.log("resp bytes: " + bytesRec + "\n" + Hex.toStringBlock(buffer));
//        }
        // close "ois" (tm)
        os.close();
        is.close();
        s.close();

    }

    /**
     * write request body to stream
     * @param os destination stream for body
     * @param is source stream
     * @param length number of bytes to transfer
     * @param chunked if true use chunked transfer encoding
     * @param createSeed if not 0 write random bytes
     */
    private static void writeRequest(OutputStream os, InputStream is, int length, boolean chunked, int createSeed, int blockSize, int sleep) throws Exception {

        if (createSeed != 0) {
            Random rand = new Random(createSeed);
            if (chunked) {
                // int chunkSize = 512; // 16384;
                byte[] chunk = new byte[blockSize];
                byte[] chunkHeader = (Integer.toString(blockSize, 16) + "\r\n").getBytes(ASCII);
                int remaining = length;
                while (remaining > blockSize) {
                    rand.nextBytes(chunk);
                    os.write(chunkHeader);
                    os.write(chunk);
                    os.write(0x0d);
                    os.write(0x0a);
                    remaining -= blockSize;
                    if (sleep > 0) {
                        Thread.sleep(sleep);
                    }
                }
                if (remaining > 0) {
                    byte[] lastChunk = new byte[remaining];
                    rand.nextBytes(lastChunk);
                    os.write(Integer.toString(remaining, 16).getBytes(ASCII));
                    os.write(0x0d);
                    os.write(0x0a);
                    os.write(lastChunk);
                    os.write(0x0d);
                    os.write(0x0a);
                }
                os.write("0\r\n\r\n".getBytes(ASCII));  // final chunk (zero bytes) and trailer
            }
            else {
                // request body  (fixed length)

                // all in one, rather memory consuming..
                // byte[] body = new byte[length];
                // rand.nextBytes(body);
                // os.write(body);

                int remaining = length;
                byte[] bodyPart = new byte[blockSize];
                while (remaining > blockSize) {
                    rand.nextBytes(bodyPart);
                    os.write(bodyPart);
                    remaining -= blockSize;
                    if (sleep > 0) {
                        Thread.sleep(sleep);
                    }
                }
                // final block
                if (remaining > 0) {
                    byte[] last = new byte[remaining];
                    rand.nextBytes(last);
                    os.write(last);
                }
            }
        }
    }

    private static byte[] readResponse(InputStream is, int length, boolean dummy, boolean chunked, int validateSeed) throws IOException {
        if (length == 0 && !chunked) {
            return null; // TODO: check: no body?
        }
        if (length > 0 && chunked) {
            throw new RuntimeException("did not expect chunked with content size..."); // TODO strange combination?
        }

        Random r = null;
        if (validateSeed > 0) {
            r = new Random(validateSeed);
        }

        if (length > 0) {
            Log.info("reading body. size=" + length);
            byte[] body = new byte[length];
            readBytes(body, is);
            if (validateSeed > 0) {
                validate(r, body);
            }
            return body;
        }
        if (chunked) {  // RFC 7230 4.1
            int total = 0;
            while (true) {  // repeat all chunks
                String chunkSize = readCRLF(is);
                int size = Integer.parseInt(chunkSize, 16);
                total += size;
                if (size == 0) {
                    readCRLFEmpty(is); // TODO: what is the trailer-part?
                    Log.info("reading chunk. total=" + total);
                    return new byte[0]; // TODO "assemble" junk later
                }
                // Log.log("reading chunk. size=" + size);
                byte[] chunk = new byte[size];
                readBytes(chunk, is);
                if (validateSeed > 0) {
                    validate(r, chunk);
                }
                readCRLFEmpty(is);
            }
        }
        throw new RuntimeException("don't know how to read body.");
    }

    /**
     * read expected CR LF.
     */
    private static void readCRLFEmpty(InputStream is) throws IOException {
        String newline = readCRLF(is);
        if (newline.length() > 0) {
            throw new RuntimeException();
        }
    }

    private static void validate(Random r, byte[] body) {
        byte[] expected = new byte[body.length];
        r.nextBytes(expected);
        if (!Arrays.equals(body, expected)) {
            throw new RuntimeException("validation failed..");
        }
    }

/*
    private static byte[] readResponse(InputStream is, int length, boolean dummy) throws IOException {
        byte[] body = null;
        if (!dummy) {
            body = new byte[length];
        }
        int bufferSize = 8192;
        byte[] buffer = new byte[bufferSize];
        int total = 0;
        int bytes;
        while ((length - bufferSize) > bufferSize && (bytes = is.read(buffer)) > 0) {
            if (!dummy) {
                System.arraycopy(buffer, 0, body, total, bytes);
            }
            total += bytes;
        }


        if (total < length) {
            throw new RuntimeException(); // TODO
        }
        return body;
    }
*/

    /*
     * read expected amount of bytes from InputStream. fail if bytes are missing
     */
    private static void readBytes(byte[] bytes, InputStream is) throws IOException {
        int length = bytes.length;
        int position = 0;
        while (position < length) {
            int read = is.read(bytes, position, length - position);
            if (read == -1) {
                throw new RuntimeException("early end of stream.");
            }
            position += read;
        }
        if (position != length) {
            throw new RuntimeException("missing bytes. pos=" + position + " length=" + length);
        }
    }

    /**
     * read ascii from input stream. expect crlf termination
     *
     * @return ascii string
     */
    private static String readCRLF(InputStream is) throws IOException {
        StringBuilder header = new StringBuilder();
        int received;
        while (true) {
            // read next header byte
            received = is.read();
            if (received == -1) { // "end of stream"
                throw new RuntimeException("unexpected end of stream.");
            }
            if (received == 0x0d) {
                received = is.read(); // read expected 0x0a
                if (received != 0x0a) {
                    throw new RuntimeException();
                }
                return header.toString(); // header finished
            }
            if (received == 0x0a) {
                throw new RuntimeException();
            }
            if (received < 31 || received > 126) {
                throw new RuntimeException("invalid character: " + received);
            }
            header.append((char) received);  // "safe" ascii cast
        }
    }

    private static class UrlParts {

        private static final String HTTP = "http";
        private static final String HTTPS = "https";

        private String scheme;
        private String host;
        private int port;
        private String path;

        public UrlParts(String url) {
            Validate.notNull(url, "url is null.");
            int schemeIdx = url.indexOf("://");
            Validate.isTrue(schemeIdx > 0, "found no scheme separator in url " + url);
            scheme = url.substring(0, schemeIdx);
            String rest = url.substring(schemeIdx + 3);
            int slashIdx = rest.indexOf("/");
            String hostPort;
            if (slashIdx == -1) {
                path = "/"; // browser would add "/", might be error.
                hostPort = rest;
            }
            else {
                path = rest.substring(slashIdx);
                hostPort = rest.substring(0, slashIdx);
            }
            int colonIndex = hostPort.indexOf(":");
            if (colonIndex == -1) {
                host = hostPort;
                if (HTTP.equals(scheme)) {
                    port = 80;
                }
                else if (HTTPS.equals(scheme)) {
                    port = 443;
                }
                else {
                    throw new RuntimeException("missing default port for scheme " + scheme);
                }
            }
            else {
                host = hostPort.substring(0, colonIndex);
                port = Integer.parseInt(hostPort.substring(colonIndex + 1));
            }
        }
    }


    private static class ResponseHeader {

        private enum Status {
            HByte,
            H0d,
            H0d0a,
            H0d0a0d,
        }

        // status of reading header bytes....

        private int statusCode;
        private String statusMessage;
        private ArrayList<String> headers;

        // -- pre parsed headers (first class, i.e. relevant for correct client handling)
//        private long date;
        private int contentLength;
        private String contentType;
        private String connection;   // "close"
        boolean close;
        private String transferEncoding; // "chunked"
        boolean chunked;

        /**
         * this is tricky:
         * response can be binary
         * response can be terminated by multiple criteria:
         * - EOS end of stream (trivial)
         * - double new line (in case of ascii response)
         * - contentLength bytes transferred (also in binary)
         *
         */
        public void fromInputStream(InputStream is) throws IOException {

            StringBuilder header = new StringBuilder();
            headers = new ArrayList<String>();
            // do this byte wise, too hard with buffers....
            Status s = Status.HByte;
            contentLength = 0;  // what if header not present?
            chunked = false;
            close = false; // http 1.1 default
            int received;

            while (true) {
                // read next header byte
                received = is.read();
                if (received == -1) { // "end of stream"
                    throw new RuntimeException(); // TODO early end of stream
                }
                if (received == 0x0d) {
                    if (s == Status.HByte) {
                        s = Status.H0d;
                        if (header.length() == 0) {
                            throw new RuntimeException(); // TODO
                        }
                    }
                    else if (s == Status.H0d0a) {
                        s = Status.H0d0a0d;
                        continue;
                    }
                    else throw new RuntimeException(); // TODO
                }
                if (received == 0x0a) {
                    if (s == Status.H0d) {
                        s = Status.H0d0a;
                        continue;
                    }
                    else if (s == Status.H0d0a0d) {
                        addHeader(header);
                        // Log.info("last header: " + header);
                        header = null;  // release to GC?
                        break; // done with header parsing....
                    }
                    else throw new RuntimeException(); // TODO
                }

                if (s == Status.H0d0a) {
                    // previous header "done"
                    addHeader(header);
                    s = Status.HByte; // continue next header
                    // Log.info("header: " + header);
                    header = new StringBuilder();
                }

                if (s == Status.HByte) {
                    if (received < 31 || received > 126) {
                        throw new RuntimeException("invalid character: " + received);
                    }
                    header.append((char)received);  // "safe" ascii cast
                }
            }
        }

        private void addHeader(StringBuilder header) {
            String h = header.toString();
            if (h.startsWith("HTTP/1.1 ")) {  // TODO: fair assumption but still: enforce that it's true
                StringTokenizer st = new StringTokenizer(h, " ");
                st.nextToken();
                statusCode = Integer.parseInt(st.nextToken());
                statusMessage = st.nextToken();
            }
            else if (h.startsWith("Content-Type")) {
                contentType = getHeaderValue(h);
            }
            else if (h.startsWith("Transfer-Encoding")) {
                transferEncoding = getHeaderValue(h);
                chunked = "chunked".equals(transferEncoding);
            }
            else if (h.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(getHeaderValue(h));
            }
            else if (h.startsWith("Connection")) {
                connection = getHeaderValue(h);
                close = "close".equals(connection);
            }
//            else if (h.startsWith("Date")) {  omitted for speed
//                date = parseDate(getHeaderValue(h));
//            }

            headers.add(h);
        }

        /** get header value from header line */
        private String getHeaderValue(String header) {
            int pos = header.indexOf(":");
            if (pos == -1) {
                throw new RuntimeException("header without ':' " + header);
            }
            while (header.charAt(pos + 1) == ' ') {  // may be more than one blank?
                pos +=1;
            }
            return header.substring(pos + 1);
        }
    }

    private static class RequestHeader {

        private String url;
        private String method;
        private ArrayList<String> headers;

        public RequestHeader(String url, String method) {
            this.url = url;
            this.method = method;
            this.headers = new ArrayList<String>();
        }

        public void addHeader(String name, String value) {
            String header = name + ": " + value;
            headers.add(header);
        }

        public byte[] getBytes() {
            StringBuilder sb = new StringBuilder();
            sb.append(method);   // RFC 7230 3.1.1.  Request Line
            sb.append(" ");
            sb.append(url);
            sb.append(" HTTP/1.1\r\n");
            for (String header : headers) {
                sb.append(header);
                sb.append("\r\n");
            }
            // final newline to finish request....
            sb.append("\r\n");

            //
            return sb.toString().getBytes(Charset.forName(ASCII));  // RFC 7230 3.2.4. ... tl;dr ... ASCII "preferred"
        }

    }

}
