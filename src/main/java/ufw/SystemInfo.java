package ufw;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLClassLoader;

public class SystemInfo {

    public static void show() {
        String os = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ", " + System.getProperty("os.version") + ")";
        String runtime = System.getProperty("java.runtime.name") + " (" + System.getProperty("java.runtime.version") + ")";
        String vm = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.version") + ")";
        Log.info("os=" + os);
        Log.info("vm=" + vm);
        Log.info("rt=" + runtime);
    }

    public static void showClassPath() {

        RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
        char sep = System.getProperty("path.separator").charAt(0);  //
        Log.info("classPath(RTMXB)=\n" + rtb.getClassPath().replace(sep, '\n'));
        // this path is not found in classLoader hierarchy
        Log.info("bootClassPath(RTMXB)=\n" + rtb.getBootClassPath().replace(sep, '\n'));
        Log.info("libraryPath(RTMXB)=\n" + rtb.getLibraryPath().replace(sep, '\n'));
        Log.info("--------");

        // TODO
        System.getProperty("java.class.path");
        System.getProperty("java.library.path");
        System.getProperty("sun.boot.library.path");
        System.getProperty("sun.boot.class.path");

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
        show();
        Log.info("--------");
        showClassPath();
    }

}
