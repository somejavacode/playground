/**
 * dump all command line args
 */
public class ShowArgs {
    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {
            System.out.println("arg " + i + " <" + args[i] + ">");
        }
    }
}