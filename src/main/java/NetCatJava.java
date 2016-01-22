import ufw.Args;
import ufw.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * java tool inspired by the unix tool netcat https://en.wikipedia.org/wiki/Netcat<br/>
 * TODO: add multi-cast
 */
public class NetCatJava {

    public static void main(String[] args) throws Exception {
        // -l <port> listen to port (server)
        // -b <address> bind address (server)
        // <host> <port> (client, no -l)
        // -of output file (if omitted use stdout)
        // -if input file (if omitted use stdin)
        // --udp use udp instead of tcp

        Args parsedArgs = new Args(args);

        // server
        int listenPort = parsedArgs.getIntValue("l");
        // client
        String host = parsedArgs.getExtraValue(0);
        int port = parsedArgs.getExtraIntValue(1);

        String outFile = parsedArgs.getValue("of");
        String inFile = parsedArgs.getValue("if");
        boolean udp = parsedArgs.hasFlag("udp");


        if (inFile != null && !new File(inFile).exists()) {
            throw new RuntimeException("input file not found: " + inFile);
        }

//        if (inFile != null && outFile != null) {
//            throw new RuntimeException("don't use input file and output file at the same time");
//        }

        if (args.length < 2) {
            Log.info("NetCatJava [--udp] -l <listenPort>|<remoteServer> <remotePort> [-of <outputFile>][-if <inputFile>]");
            return;
        }

        if (udp) {
            DatagramSocket udpSocket = null;
            int udpMax = 1024;
            int remotePort = port;

            if (listenPort > 0) {
                udpSocket = new DatagramSocket(listenPort);
                byte[] buf = new byte[udpMax];
                DatagramPacket recPacket = new DatagramPacket(buf, udpMax);
                udpSocket.receive(recPacket);
                remotePort = recPacket.getPort();  // is this source port?

            }
            else {
                udpSocket = new DatagramSocket(port);
                byte[] buf = new byte[udpMax];
                DatagramPacket sendPacket = new DatagramPacket(buf, udpMax);
                // TODO

            }


        }

        else if (listenPort > 0) {
            // tcp server
            ServerSocket socket = new ServerSocket(listenPort);
            // "cheap" single accept
            Socket s = socket.accept();
            handleSocketStreams(outFile, inFile, s);
        }
        else {
            int connectTimeout = 5000;  // 5s
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            Socket s = new Socket();
            // Log.debug("start connect host=" + host + " port=" + port);
            s.connect(socketAddress, connectTimeout);

            if (inFile != null || outFile != null) {
                // no timeout for interactive "console" case
                int socketTimeout = 5000;  // 5s
                s.setSoTimeout(socketTimeout);
            }

            handleSocketStreams(outFile, inFile, s);
        }

    }

    private static void handleSocketStreams(String outFile, String inFile, Socket s) throws IOException {
        Thread readerThread = null;
        Thread writerThread = null;

        if (outFile == null) {
            readerThread = new Thread(new StreamPipe(s.getInputStream(), System.out, true), "reader");
            readerThread.start();
        }
        else {
            readerThread = new Thread(new StreamPipe(s.getInputStream(), new FileOutputStream(outFile), false), "reader");
            readerThread.start();
        }

        if (inFile == null) {
            // read every line from console and write to socket
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(s.getOutputStream());

            String line;
            while ((line = br.readLine()) != null) { // write in main thread
                writer.write(line);
                writer.write("\n"); // this is possibly wrong. cannot find out what separator was found with readLine()
                writer.flush();
            }
        }
        else {
            writerThread = new Thread(new StreamPipe(new FileInputStream(inFile), s.getOutputStream(), false), "writer");
            writerThread.start();
        }

        // readerThread.join();  // TODO
        // writerThread.join();
    }

    private static class StreamPipe implements Runnable {

        private InputStream is;
        private OutputStream os;
        private boolean console;

        public StreamPipe(InputStream is, OutputStream os, boolean console) {
            this.is = is;
            this.os = os;
            this.console = console;
        }

        @Override
        public void run() {
            int buffSize = 4096;
            byte[] buffer = new byte[buffSize];
            int readBytes;
            try {
                while ((readBytes = is.read(buffer)) > 0) {
                    os.write(buffer, 0, readBytes);
                    os.flush();
                }
            }
            catch (Exception e) {
                if (!console) {
                    // mute close warning in console mode
                    Log.warn("stream copy done with exception.", e);
                }
            }

            try {
                os.close();
            }
            catch (IOException e1) {
                Log.warn("stream close failed.", e1);
                return;
            }
            if (console) {
                System.exit(0); // well, this is hardcore...
            }
            Log.debug("stream copy done.");
        }
    }


}
