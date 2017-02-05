package test.ufw;

import org.junit.Test;
import ufw.Log;
import ufw.RuntimeInfo;
import ufw.SystemInfo;

public class RuntimeInfoTest {

    @Test
    public void testPid() throws Exception {
        Log.info("PID=" + RuntimeInfo.getPid());
    }

    @Test
    public void testUptime() throws Exception {
        Log.info("Uptime=" + RuntimeInfo.getUptime() + "ms");
    }
}
