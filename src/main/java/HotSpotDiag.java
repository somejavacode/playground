import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import ufw.Log;

import java.lang.management.ManagementFactory;
import java.util.List;

public class HotSpotDiag {

    public static void main(String[] args) {
        // HotSpotDiagnostic hsd = new HotSpotDiagnostic();  // this also works, too easy?
        HotSpotDiagnosticMXBean hsd = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);

        if (!System.getProperty("java.vm.name").contains("OpenJDK")) {
            // for OpenJDK "java.lang.IllegalArgumentException: VM option "UnlockCommercialFeatures" does not exist"
            hsd.setVMOption("UnlockCommercialFeatures", "true");  // OracleJDK : list will be extended 19 -> 24,
                                                                  // OpenJDK: 18 (without UnlockCommercialFeatures)
        }

        // fails with "java.lang.IllegalArgumentException: VM Option "UnlockDiagnosticVMOptions" is not writeable"
        // hsd.setVMOption("UnlockDiagnosticVMOptions", "true");
        // command line "-XX:+UnlockDiagnosticVMOptions" changes nothing

        List<VMOption> options = hsd.getDiagnosticOptions();
        Log.info("total: " + options.size());
        for (VMOption o : options) {
            Log.info(o);  // each line looks like this "HeapDumpBeforeFullGC value: false  origin: DEFAULT (read-write)"
        }
        // compared with http://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html
        // some match -XX options, many seem to be "undocumented":
        // HeapDumpAfterFullGC, HeapDumpBeforeFullGC
        // CMSAbortablePrecleanWaitMillis, CMSWaitDuration, CMSTriggerInterval
        // PrintGCID, PrintClassHistogramBeforeFullGC, PrintClassHistogramAfterFullGC
        // (commercial)
        // MemoryRestriction

    }

}
