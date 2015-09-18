package test.ufw;

import org.junit.Test;
import ufw.Log;
import ufw.SystemInfo;

public class TestSystemInfo {

    @Test
    public void test() throws Exception {
        SystemInfo.show();
        Log.info("---------");
        SystemInfo.showClassPath();
    }
}
