import ufw.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * my first udp socket.
 * <p>
 * tested OK:
 * <p>
 * start sever: <code>java UdpTest 2222</code>
 * <p>
 * start client:  <code>java UdpTest localhost 2222</code>
 */

public class UdpTest {

    // sample code..
    // https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html

    // safe udp packet size for internet: 512?
    // http://stackoverflow.com/questions/1098897/what-is-the-largest-safe-udp-packet-size-on-the-internet

    // limit test with: "java UdpTest localhost 2222 512"
    // local package size limit is beyond 16kByte (only limited by udpMax of server). need to test with switch or with "internet".


    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            Log.info("UdpTest <listenPort> :start server");
            Log.info("UdpTest <remoteHost> <remotePort> [size]: start client.");
            return;
        }
        int udpMax = 4096;
        byte value = (byte) 0xA5;

        if (args.length == 1) {
            // server
            int listenPort = Integer.parseInt(args[0]);
            DatagramSocket udpSocket =  new DatagramSocket(listenPort);
            Log.info("start receive. port=" + listenPort);
            while (true) {
                byte[] buf = new byte[udpMax];
                DatagramPacket recPacket = new DatagramPacket(buf, udpMax);
                udpSocket.receive(recPacket);
                //  Log.info("received: " + recPacket); // what will toString be? useless.
                String message = null;
                if (recPacket.getLength() < 50) {
                    message = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
                }
                else {
                    for (int i = 0; i < recPacket.getLength(); i++) {
                        if (value != recPacket.getData()[recPacket.getOffset() + i]) {
                            throw new RuntimeException("got wrong byte. pos=" + i);
                        }
                    }
                    message = recPacket.getLength() + "bytes";
                }
                Log.info("received: " + recPacket.getAddress() + ":" + recPacket.getPort() + " msg=" + message);
            }
            // there is no "close". server is never "done".
        }
        if (args.length >= 2) {
            // client
            String remoteHost = args[0];
            InetAddress remoteAddress = InetAddress.getByName(remoteHost);
            int remotePort = Integer.parseInt(args[1]);
            // int localPort = remotePort;
            DatagramSocket udpSocket =  new DatagramSocket();  // "any" local port. don't care.
            if (args.length > 2) {
                int size = Integer.parseInt(args[2]);
                byte[] buf = new byte[size];
                for (int i = 0; i < size; i++) { // fill array
                    buf[i] = value;
                }
                DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, remoteAddress, remotePort);
                udpSocket.send(sendPacket);
            }

            else { // send 5 "text" packages
                for (int i = 0; i < 5; i++) {
                    if (i > 0) {
                        Thread.sleep(1000);
                    }
                    String msg = "hello_" + i;
                    byte[] buf = msg.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, remoteAddress, remotePort);
                    udpSocket.send(sendPacket);
                    Log.info("sent: " + msg);
                }
            }
        }
    }

}
