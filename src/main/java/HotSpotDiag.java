import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import ufw.Log;

import java.lang.management.ManagementFactory;
import java.util.List;

public class HotSpotDiag {

    public static void main(String[] args) {
        // HotSpotDiagnostic hsd = new HotSpotDiagnostic();  // this also works, too easy?
        HotSpotDiagnosticMXBean hsd = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        hsd.setVMOption("UnlockCommercialFeatures", "true");  // list will be extended 19 -> 24

        List<VMOption> options = hsd.getDiagnosticOptions();
        Log.info("total: " + options.size());
        for (VMOption o : options) {
            Log.info(o);  // some match -XX options, some seem to be "new".
        }

    }

}
