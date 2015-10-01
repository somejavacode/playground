import sun.misc.Version;
import ufw.Log;
import ufw.SystemInfo;

public class MiscVersionTest {

    public static void main(String[] args) {

        Log.info("sun.misc.Version.jdkMajorVersion =<" + Version.jdkMajorVersion() + ">");
        Log.info("sun.misc.Version.jdkMinorVersion =<" + Version.jdkMinorVersion() + ">");
        Log.info("sun.misc.Version.jdkMicroVersion =<" + Version.jdkMicroVersion() + ">");
        Log.info("sun.misc.Version.jdkUpdateVersion =<" + Version.jdkUpdateVersion() + ">");
        Log.info("sun.misc.Version.jdkBuildNumber =<" + Version.jdkBuildNumber() + ">");
        Log.info("sun.misc.Version.getJdkSpecialVersion =<" + Version.getJdkSpecialVersion() + ">");

        Log.info("sun.misc.Version.jvmMajorVersion =<" + Version.jvmMajorVersion() + ">");
        Log.info("sun.misc.Version.jvmMinorVersion =<" + Version.jvmMinorVersion() + ">");
        Log.info("sun.misc.Version.jvmMicroVersion =<" + Version.jvmMicroVersion() + ">");
        Log.info("sun.misc.Version.jvmUpdateVersion =<" + Version.jvmUpdateVersion() + ">");
        Log.info("sun.misc.Version.jvmBuildNumber =<" + Version.jvmBuildNumber() + ">");
        Log.info("sun.misc.Version.getJvmSpecialVersion =<" + Version.getJvmSpecialVersion() + ">");

        Log.info("------\n");

        Version.print(System.out);  // this is same like "java -version"

        // according to line 101 of http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/tip/src/share/classes/sun/misc/Version.java.template
        // launcher_name=java?, "java.version"
        // "java.runtime.name" "java.runtime.version"
        // "java.vm.name", "java.vm.version", "java.vm.info"

        Log.info("--------\n");
        SystemInfo.show();
    }
}
