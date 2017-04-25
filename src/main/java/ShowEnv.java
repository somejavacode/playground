import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * dump all environment variables sorted by key
 */
public class ShowEnv {
    public static void main(String[] args) {

        Map<String, String> p = System.getenv();
        ArrayList<String> names = new ArrayList<>(p.keySet());
        Collections.sort(names);
        for (String name : names) {
            System.out.println(name + "=" + p.get(name));
        }
    }
}