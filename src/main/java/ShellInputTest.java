import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellInputTest {

    public static void main(String[] args) throws Exception {
        System.out.print("input: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("got: " + line);
            System.out.print("input: ");
        }
    }
}
