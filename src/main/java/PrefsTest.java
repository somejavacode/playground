import ufw.Log;

import java.util.prefs.Preferences;

public class PrefsTest {
    public static void main(String[] args) {
        // this _brain-dead_ Preferences class emulates windows _registry pain_
        // in command shell: "reg query  HKCU\Software\JavaSoft\Prefs"
        Preferences preferences = Preferences.userRoot();
        Log.info("testing " + preferences.get("does not exist", "default"));
    }

}
