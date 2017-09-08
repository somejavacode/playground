/**
 * java console encoding is tedious (at least with windows with JDK7).
 *
 * some hints:
 * http://stackoverflow.com/questions/14030811/system-out-character-encoding
 * http://superuser.com/questions/482018/how-to-set-default-charset-in-windows-cmd
 * http://ss64.com/nt/chcp.html
 *
 * final conclusion, for windows you need to control 3 "variables":
 * - java file.encoding (e.g. -Dfile.encoding=cp850)
 * - cmd.exe codepage (e.g. chcp 1252)
 * - cmd.exe font (truetype font improves things: cmd.exe - properties - font - "Lucidia Console")
 *
 * further notes
 * - java defaults are "rather broken" (using same property for console and file might be one problem)
 * - System.console() might fix some things but is not (or cannot be) used
 */
public class ConsoleEncodingTest {

    public static void main(String[] args) {
        System.out.println("file.encoding=" + System.getProperty("file.encoding"));
        System.out.println("sun.stdout.encoding=" + System.getProperty("sun.stdout.encoding"));
        System.out.println("Hell\u00f6 \u00dcmlauts.");
        System.out.println("\u00c4 \u00e4 \u00d6 \u00f6 \u00dc \u00fc \u00df"); // Ä, ä, Ö, ö, Ü, ü, ß
        if (args.length > 0) {
            System.out.println("args[0]=" + args[0]); // test "round trip"
        }
        System.out.println("arabic \u06ad \u06a7");  // non latin

        // System.console().printf("using System.console(): Hell\u00f6 \u00dcmlauts.");
    }
}