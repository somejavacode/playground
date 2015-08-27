import ufw.Log;

import java.net.InetAddress;

public class Ping {

     // http://stackoverflow.com/questions/2448666/how-to-do-a-true-java-ping-from-windows

    // test 2015-06-04 windows (xp sp2 and seven sp1) failed: results are different from OS "ping.exe"
    // for one machine: OS ping was OK, java-ping failed
    // assume that on windows java is not able to send "true" ICMP packages
    // need to test linux.

    public static void main(String[] args) throws Exception {

        String target = args[0];
        // String target = "10.0.0.6";

        int timeout = 5000;
        if (args.length > 2) {
            timeout = Integer.parseInt(args[2]);
        }
        int delay = 0;
        if (args.length > 1) {
            delay = Integer.parseInt(args[1]);
        }
        InetAddress address = InetAddress.getByName(target);

        printReachable(address, timeout);
        if (delay > 0) {
            while (true) {
                Thread.sleep(delay * 1000);
                printReachable(address, timeout);
            }
        }
    }

    private static void printReachable(InetAddress target, int timeout) throws Exception {
        boolean reachable = target.isReachable(timeout);
        Log.info(target + " isReachable=" + reachable);
    }

}
