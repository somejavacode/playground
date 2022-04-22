package ufw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * just a quick hack for (usually tedious) command line arguments parsing.
 * <p>
 * reduced syntax: --flag1 -option1 value1 -option2 value2 --flag2 extraValue1 extraValue2
 * <p>
 * Rules:
 *  There are flags and options followed by extra values.
 *  option names start with "-" followed by option values not starting with "-".
 *  flags names start with "--".
 *  extra values must not start with "-".
 */
public class Args {

    public static final String OPTION_PREFIX = "-";
    public static final String FLAG_PREFIX = "--";

    private HashMap<String, String> parsedArgs = new HashMap<String, String>();
    private ArrayList<String> extraArgs = new ArrayList<String>();
    private HashSet<String> flags = new HashSet<>();

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

    public String getValue(String name, String defaultValue) {
        String result = parsedArgs.get(name);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    public int getIntValue(String name) {
        return getIntValue(name, 0);
    }

    public int getIntValue(String name, int defaultValue) {
        String intStr = parsedArgs.get(name);
        if (intStr == null) {
            return defaultValue;
        }
        return Integer.parseInt(intStr);
    }

}
