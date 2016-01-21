package ufw;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.TimeZone;

public class SystemInfo {

    public static void show() {
        show(false);
    }
    public static void show(boolean extended) {
        String os = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ", " + System.getProperty("os.version") + ")";
        String runtime = System.getProperty("java.runtime.name") + " (" + System.getProperty("java.runtime.version") + ")";
        String vm = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.info") + ")";
        Log.info("os=" + os);
        Log.info("vm=" + vm);
        Log.info("rt=" + runtime);
        if (extended) {
            // some properties that usually make software system dependent
            showProperty("file.encoding");
            showProperty("user.language");
            showProperty("user.language.format");
            // showProperty("user.timezone");  // why is this null? this is only for overriding
            Log.info("timezone=" + TimeZone.getDefault().getID());
            showProperty("user.dir");  // current working directory
            showProperty("user.home");
            showProperty("java.home");
        }
    }

    private static void showProperty(String name) {
        Log.info(name + "=" + System.getProperty(name));
    }

    public static void showClassPath() {

        char sep = System.getProperty("path.separator").charAt(0);  // bit ugly but works

//        RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
//        Log.info("classPath(RTMXB)=\n" + rtb.getClassPath().replace(sep, '\n'));
//        // this path is not found in classLoader hierarchy
//        Log.info("bootClassPath(RTMXB)=\n" + rtb.getBootClassPath().replace(sep, '\n'));
//        Log.info("libraryPath(RTMXB)=\n" + rtb.getLibraryPath().replace(sep, '\n'));
//        Log.info("libraryPath(RTMXB)=\n" + rtb.getLibraryPath().replace(sep, '\n'));

        // system properties deliver same results like RuntimeMXBean (plus extra "sun.boot.library.path")
        Log.info("------- java.class.path:\n" + System.getProperty("java.class.path").replace(sep, '\n'));
        Log.info("------- java.library.path:\n" + System.getProperty("java.library.path").replace(sep, '\n'));
        Log.info("------- sun.boot.class.path:\n" + System.getProperty("sun.boot.class.path").replace(sep, '\n'));
        Log.info("------- sun.boot.library.path:\n" + System.getProperty("sun.boot.library.path").replace(sep, '\n'));
        Log.info("-------");

        // Log.info("java.system.class.loader=" + System.getProperty("java.system.class.loader")); always null?
        ClassLoader cls = ClassLoader.getSystemClassLoader();
        dumpClassLoader("system", cls);

        ClassLoader tls = Thread.currentThread().getContextClassLoader();
        if (tls != cls) {
            dumpClassLoader("context", cls);
        }
        else {
            Log.info("contextClassLoader is same as systemClassLoader");
        }
        ClassLoader ccl = SystemInfo.class.getClassLoader();
        if (ccl != cls) {
            dumpClassLoader("this", cls);
        }
        else {
            Log.info("'this'ClassLoader is same as systemClassLoader");
        }
    }

    private static void dumpClassLoader(String name, ClassLoader cls) {
        URL[] urls = ((URLClassLoader) cls).getURLs();
        Log.info(name + "=" + cls);  // what toString will do?
        for (URL url : urls) {
            Log.info("url: " + url.toString());
        }
        // check parents...
        String prefix = "parent ";
        while (cls.getParent() != null) {
            cls = cls.getParent();
            Log.info(prefix + name + "=" + cls);
            urls = ((URLClassLoader) cls).getURLs();
            for (URL url : urls) {
                Log.info(prefix + "url: " + url.toString());
            }
            prefix += "parent ";
        }
    }

    public static void main(String[] args) {
        show(true);
        Log.info("-------");
        showClassPath();
    }

}
