import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * socket that accepts connect and "does nothing"
 */
public class StickySocket {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("usage: StickySocket port [bindAddress]");
            return;
        }
        int listenPort = Integer.parseInt(args[0]);
        String bindAddress = null;
        if (args.length > 1) {
            bindAddress = args[1];
        }

        InetAddress bind = bindAddress == null ? null : InetAddress.getByName(bindAddress);
        int backlog = 50; // default value.
        ServerSocket socket = new ServerSocket(listenPort, backlog, bind);
        // "cheap" single threaded accept
        System.out.println("listening to " + listenPort);
        while (true) {
            Socket s = socket.accept();
            System.out.println("connected");
        }
    }
}
