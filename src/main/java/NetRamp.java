import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * NetRamp is a tool to "waste" sockets. It allows you to test application and OS behavior when running out of sockets.
 * <p/>
 * By default NetRamp starts server on localhost port 777, connect 100 clients and waits 600s.
 * <p/>
 * on linux check status with "netstat -n -t
 */
public class NetRamp {

    public static void main(String[] args) throws Exception {

        int count = 100;
        if (args.length > 0) {
            count = Integer.parseInt(args[0]);
        }

        int sleep = 600;
        if (args.length > 1) {
            sleep = Integer.parseInt(args[1]);
        }

        int port = 7777;
        if (args.length > 2) {
            port = Integer.parseInt(args[2]);
        }

        String mode = "CS";
        if (args.length > 3) {
            mode = args[3];
        }

        String address = "127.0.0.1";
        if (args.length > 4) {
            address = args[4];
        }

        if (mode.toLowerCase().contains("s")) {
            InetAddress bind = InetAddress.getByName(address);
            int backlog = 50; // default value.
            ServerSocket socket = new ServerSocket(port, backlog, bind);
            Thread acceptor = new Thread(new Acceptor(socket), "acceptor");
            acceptor.setDaemon(true);
            acceptor.start();
            System.out.println("waiting for connections on port " + port);
        }

        if (mode.toLowerCase().contains("c")) {
            int connectTimeout = 5000; // 5s
            SocketAddress socketAddress = new InetSocketAddress(address, port);
            for (int i = 0; i < count; i++) {
                Socket s = new Socket();
                try {
                    s.connect(socketAddress, connectTimeout);
                }
                catch (Exception e) {
                    System.out.println("connect number " + i + " failed");
                    throw e;
                }
            }
            System.out.println("connected " + count + " sockets to " + address +  " port " + port);
        }
        System.out.println("sleeping " + sleep + "s");
        Thread.sleep(sleep * 1000);
    }

    private static class Acceptor implements Runnable {

        private ServerSocket socket;

        public Acceptor(ServerSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    socket.accept();
                }
                catch (Exception e) {
                    System.out.println("accept failed.");
                    e.printStackTrace();
                }
            }
        }
    }
}
