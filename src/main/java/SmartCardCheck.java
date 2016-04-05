import ufw.Hex;
import ufw.Log;

import javax.smartcardio.Card;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.util.List;

public class SmartCardCheck {

    public static void main(String[] args) throws Exception {

        // show the list of available terminals
        TerminalFactory factory = TerminalFactory.getDefault();

        List<CardTerminal> terminals = null;

        try {
            // no proper way to find out that card reader is not plugged in (or does not exist)?
            // terminals.isEmpty() not working properly..
            // this throws: sun.security.smartcardio.PCSCException: SCARD_E_NO_READERS_AVAILABLE
            terminals = factory.terminals().list();
        }
        catch (Exception e) {
            // could "parse" exception message in stack for unspecified text but this is "unclean".
            Throwable logEx = e.getCause() == null ? e : e.getCause();
            Log.info("found no terminals (exception: " + logEx + ")");
            return;
        }

        if (terminals.size() > 1) {
            Log.info("found " + terminals.size() + " terminals (using first): " + terminals);
        }

        CardTerminal terminal = terminals.get(0);
        Log.info("using terminal: " + terminal);

        // establish a connection with the card
        Card card = null;

        try {
            //  the protocol to use ("T=0", "T=1", or "T=CL"), or "*" to connect using any available protocol.
            card = terminal.connect("*");
        }
        catch (CardNotPresentException ex) {
            Log.info("no card present.");
            return;
        }

        Log.info("card: " + card);  // not that much info in toString(). reader, protocol, state

        // list of ATRs: http://ludovic.rousseau.free.fr/softwares/pcsc-tools/smartcard_list.txt
        Log.info("card ATR bytes=" + Hex.toString(card.getATR().getBytes()));
        Log.info("card ATR historical bytes=" + Hex.toString(card.getATR().getHistoricalBytes()));


//        CardChannel channel = card.getBasicChannel();
//        byte[] c1 = {0x00, 0x00, 0x00, 0x00};
//        ResponseAPDU r = channel.transmit(new CommandAPDU(c1));
//        Log.info("response: " + Hex.toString((r.getBytes())));
//        card.disconnect(false);
    }
}
