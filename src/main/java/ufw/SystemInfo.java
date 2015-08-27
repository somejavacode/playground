package ufw;

public class SystemInfo {

    public static void show() {
        String os = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ", " + System.getProperty("os.version") + ")";
        String runtime = System.getProperty("java.runtime.name") + " (" + System.getProperty("java.runtime.version") + ")";
        String vm = System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.version") + ")";
        Log.info("os=" + os);
        Log.info("vm=" + vm);
        Log.info("rt=" + runtime);
    }

    public static void main(String[] args) {
        show();
    }

}
