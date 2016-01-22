package ufw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * just a quick hack for (usually tedious) command line arguments parsing.<br/>
 * reduced syntax: --flag1 -option1 value1 -option2 value2 --flag2 extraValue1 extraValue2<br/>
 * Rules: argument names start with "-". argument values and extra values must not start with "-".<br/>
 */
// TODO: think about api with default values.
public class Args {

    public static final String OPTION_PREFIX = "-";
    public static final String FLAG_PREFIX = OPTION_PREFIX + OPTION_PREFIX; // "--"

    private HashMap<String, String> parsedArgs = new HashMap<String, String>();
    private ArrayList<String> extraArgs = new ArrayList<String>();
    private HashSet<String> flags = new HashSet<String>();

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
                if (value.startsWith(OPTION_PREFIX)) {
                    throw new RuntimeException("did not expect argument name or flag. expected extra parameters. got: " + value);
                }
                extraArgs.add(args[i]);
                continue;
            }
            if (name == null) {
                if (!args[i].startsWith(OPTION_PREFIX)) {
                    extra = true;  // start with extra args
                    extraArgs.add(args[i]);
                    continue;
                }
                if (args[i].startsWith(FLAG_PREFIX)) {
                    flags.add(args[i].substring(FLAG_PREFIX.length()));
                }
                else {
                    name = args[i].substring(OPTION_PREFIX.length());
                }
            }
            else { // name != null
                String value = args[i];
                if (value.startsWith(OPTION_PREFIX)) {
                    throw new RuntimeException("did not expect argument name or flag. expected a value. got: " + value);
                }
                parsedArgs.put(name, value);
                name = null;
            }
        }
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
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
