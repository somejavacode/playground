import ufw.Log;

import java.util.prefs.Preferences;

public class PrefsTest {

    public static void main(String[] args) throws Exception {
        // this _brain-dead_ Preferences class emulates windows _registry pain_
        // in command shell: "reg query HKCU\Software\JavaSoft\Prefs /s"
        Preferences preferences = Preferences.userRoot();

        // requires admin rights for modification
        // HKCM\Software\JavaSoft\Pref
        // Preferences preferences = Preferences.systemRoot();

        Log.info("testing " + preferences.get("does not exist", "default"));
        // create if missing
        preferences = preferences.node("test");
        if (preferences.getInt("testIntKey", -1) == -1) {
            Log.info("testing create");
            // all types are stored as REG_SZ (byte array as base64)
            // to create case sensitive String: upper case is "escaped" ("A" > "/A" and  / > "\/")
            // unicode is escaped "ö" > "/u00f6"
            // umlaut key creates weird encoding
            preferences.put("testStringKey", "testString/Valueöß");
            preferences.putInt("testIntKey", 999);
            preferences.putInt("testIntKeyÜ", Integer.MAX_VALUE);
            preferences.putByteArray("testByteKey", new byte[] {11, 22, 33, 44, 55});
            preferences.sync();
            // check result with "reg query HKCU\Software\JavaSoft\Prefs\test"
        }
        else {
            // delete
            Log.info("testing delete");
            preferences.removeNode();
            // preferences.sync(); // cannot sync removed node
        }
    }

}
