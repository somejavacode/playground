import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * dump all system properties sorted by key
 */
public class ShowSystemProps {
    public static void main(String[] args) {
        Properties p = System.getProperties();
        ArrayList<String> names = new ArrayList<String>(p.stringPropertyNames());
        Collections.sort(names);
        for (String name : names) {
            System.out.println(name + "=" + p.getProperty(name));
        }
    }
}