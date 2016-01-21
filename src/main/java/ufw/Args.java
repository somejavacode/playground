package ufw;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * just a quick hack for (usually tedious) command line arguments parsing.<br/>
 * reduced syntax: -option1 value1 -option2 value2 extraValue1 extraValue2<br/>
 * Rules: argument names start with "-". argument values and extra values must not start with "-".
 *
 * TODO: think about api with default values.
 */
public class Args {

    public static final String PREFIX = "-";

    private HashMap<String, String> parsedArgs = new HashMap<String, String>();
    private ArrayList<String> extraArgs = new ArrayList<String>();

    /**
     * create Args (parse arguments)
     *
     * @param args main args
     */
    public Args(String[] args) {
        String name = null;
        boolean extra = false;
        for (int i = 0; i < args.length; i++) {
            if (extra) {
                String value = args[i];
                if (value.startsWith(PREFIX)) {
                    throw new RuntimeException("did not expect argument name. expected extra parameters. got: " + value);
                }
                extraArgs.add(args[i]);
                continue;
            }
            if (name == null) {
                if (!args[i].startsWith(PREFIX)) {
                    extra = true;  // start with extra args
                    extraArgs.add(args[i]);
                    continue;
                }
                name = args[i].substring(PREFIX.length());
            }
            else { // name != null
                String value = args[i];
                if (value.startsWith(PREFIX)) {
                    throw new RuntimeException("did not expect argument name. expected a value. got: " + value);
                }
                parsedArgs.put(name, value);
                name = null;
            }
        }
    }

    public String getExtraValue(int pos) {
        if (extraArgs.size() > pos) {
            return extraArgs.get(pos);
        }
        return null;
    }

    public int getExtraIntValue(int pos) {
        String intStr = getExtraValue(pos);
        if (intStr == null) {
            return 0;
        }
        return Integer.parseInt(intStr);
    }

    public String getValue(String name) {
        return parsedArgs.get(name);
    }

    public int getIntValue(String name) {
        String intStr = parsedArgs.get(name);
        if (intStr == null) {
            return 0;
        }
        return Integer.parseInt(intStr);
    }

}
