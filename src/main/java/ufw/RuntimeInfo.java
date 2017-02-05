package ufw;

import java.lang.management.ManagementFactory;

public class RuntimeInfo {

    /**
     * @return PID of current VM
     */
    public static int getPid() {
        // ugly, but there is no better way with standard API
        return Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }

    /**
     * @return uptime in ms
     */
    public static long getUptime() {
        return System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
    }

}
