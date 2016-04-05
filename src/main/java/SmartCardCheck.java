import ufw.Hex;
import ufw.Log;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import java.util.Arrays;
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
            // javadoc "protocol": the protocol to use ("T=0", "T=1", or "T=CL"), or "*" to connect using any available protocol.
            card = terminal.connect("*");
        }
        catch (CardNotPresentException ex) {
            Log.info("no card present. waiting 20s");
            terminal.waitForCardPresent(20000);
        }
        try {
            card = terminal.connect("*");
        }
        catch (CardNotPresentException ex) {
            Log.info("no card present.");
            return;
        }

        Log.info("card: " + card);  // not that much info in toString(). reader, protocol, state

        // list of ATRs: http://ludovic.rousseau.free.fr/softwares/pcsc-tools/smartcard_list.txt
        byte[] atr = card.getATR().getBytes();
        Log.info("card ATR bytes=" + Hex.toString(atr));
        // "historical bytes" are contained in "bytes"
        // Log.info("card ATR historical bytes=" + Hex.toString(card.getATR().getHistoricalBytes()));

        // Austrian "e-Card" special Version of Starcos 3.1
        byte[] sv1 = Hex.fromString("3BBD18008131FE45805102670414B10101020081053D");
        // Austrian "e-Card" of the 4th generation.
        byte[] sv2 = Hex.fromString("3BDF18008131FE588031B05202046405C903AC73B7B1D422");

        if (Arrays.equals(atr, sv1) ||Arrays.equals(atr, sv2)) {
            // try some commands...
            Log.info("got e-card. try some commands.");
            CardChannel channel = card.getBasicChannel();

            processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x04, 0x00, Hex.fromString("D040000017010101"), 0xff));

            processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x02, 0x04, Hex.fromString("EF01"), 0xff));

            ResponseAPDU resp = processAPDU(channel, new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 0xff));

            byte[] data = resp.getData();
            Log.info("asn1 data from card: " + Hex.toString(data));
            // TODO: asn1 decoding

            card.disconnect(false);
        }
    }

    private static ResponseAPDU processAPDU(CardChannel channel, CommandAPDU apdu) throws Exception {
        Log.info("request:  " + Hex.toString(apdu.getBytes()));
        ResponseAPDU r = channel.transmit(apdu);
        Log.info("response: " + Hex.toString((r.getBytes())));
        if (r.getSW() == 0x9000 || r.getSW1() == 0x61 || r.getSW1() == 0x62 || r.getSW1() == 0x63) {
            return r;
        }
        throw new RuntimeException("invalid response status: " + r.getSW());
    }
}
