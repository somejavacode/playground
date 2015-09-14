import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * testing font rendering "effects" with certain italics fonts on OpenJDK (i.e. with FreeType library)
 */
public class FontTest extends JFrame {

    public FontTest(String fontName, int size) {

        String textMessage = "Testing: // **";
        Font font = new Font(fontName, Font.PLAIN, size);
        JLabel textLabel = new JLabel(textMessage);
        textLabel.setFont(font);

        Font font2 = new Font(fontName, Font.ITALIC, size);
        JLabel textLabel2 = new JLabel(textMessage);
        textLabel2.setFont(font2);

        getContentPane().setLayout(new FlowLayout());
        getContentPane().add(textLabel);
        getContentPane().add(textLabel2);
        setVisible(true);
    }

    public static void main(String[] args) {
        // listFonts();
        String fontName = "Lucida Console";
        if (args.length > 0) {
            fontName = args[0];
        }
        JFrame frame = new FontTest(fontName, 11);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(100, 65));
        // frame.pack();
        frame.setVisible(true);
    }

    public static void listFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();
        for (Font font : fonts) {
            System.out.println(font.getFontName());
        }

    }
}