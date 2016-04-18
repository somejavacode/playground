import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.util.ASN1Dump;
import ufw.Hex;
import ufw.Log;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class SimCardCheck {

    public static void main(String[] args) throws Exception {

        // show the list of available terminals
        TerminalFactory factory = TerminalFactory.getDefault();

        List<CardTerminal> terminals = null;

        try {
            // no proper way to find out that card reader (aka terminal) is not plugged in (or does not exist)?
            // terminals.isEmpty() not always working ..
            // this throws: sun.security.smartcardio.PCSCException: SCARD_E_NO_READERS_AVAILABLE
            terminals = factory.terminals().list();
        }
        catch (Exception e) {
            // could "parse" exception message in stack for unspecified text but this is "unclean".
            Throwable logEx = e.getCause() == null ? e : e.getCause();
            Log.info("found no terminals (exception: " + logEx + ")");
            return;
        }

        if (terminals.isEmpty()) {
            Log.info("found no terminal."); // second possible "no terminal" case.
            return;
        }

        if (terminals.size() > 1) {
            Log.info("found " + terminals.size() + " terminals (using first): " + terminals);
        }

        CardTerminal terminal = terminals.get(0);
        Log.info("using terminal: " + terminal);

        Card card = null;
        boolean repeat = true;  // endless
        int cardWait = 20000; // 20s
        while (repeat) {
            // wait for card if necessary
            if (!terminal.isCardPresent()) {
                Log.info("waiting for card...");
                terminal.waitForCardPresent(cardWait);
            }

            // exit if no card was found in time
            if (!terminal.isCardPresent()) {
                Log.info("no card present.");
                return;
            }

            // connect to card
            // javadoc "protocol": the protocol to use ("T=0", "T=1", or "T=CL"), or "*" to connect using any available protocol.
            card = terminal.connect("*");  // usually get "0"

            Log.info("card: " + card); // not that much info in toString(). reader, protocol, state

            // list of ATRs: http://ludovic.rousseau.free.fr/softwares/pcsc-tools/smartcard_list.txt
            byte[] atr = card.getATR().getBytes();
            Log.info("card ATR bytes=" + Hex.toString(atr));
            // "historical bytes" are contained in "bytes"
            // Log.info("card ATR historical bytes=" + Hex.toString(card.getATR().getHistoricalBytes()));

            // Log.setLevel(Log.Level.INFO);  // set DEBUG to get all detail data (PDUs, full ASN1)

            CardChannel channel = card.getBasicChannel();

            String owner = "";
            // try some AIDs
            ResponseAPDU r;
            // select MF
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("3F00"), 0x00), false);
            // 9F16  = 16 bytes... SW2 .. length
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xC0, 0x00, 0x00, r.getSW2()), false);
            Log.info("MF content: " + Hex.toString(r.getData()));

//            // select ICCID
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("2FE2"), 0x00), false);
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xC0, 0x00, 0x00, r.getSW2()), false);
//            Log.info("select ICCID response: " + Hex.toString(r.getData()));
//            // file size
//            byte size = r.getData()[3];  // knowing, will be 0x0A bytes (Note: code assumes < 256)
//            // read ICCID
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xB0, 0x00, 0x00, size), false);
//            Log.info("ICCID content: " + Hex.toString(r.getData()));


//            // select IMSI
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("6F07"), 0x00), false);
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xC0, 0x00, 0x00, r.getSW2()), false);
//            Log.info("select IMSI response: " + Hex.toString(r.getData()));
//            // file size
//            byte size = r.getData()[3];
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xB0, 0x00, 0x00, size), false);
//            Log.info("IMSI content: " + Hex.toString(r.getData()));



            // https://en.wikipedia.org/wiki/Mobile_country_code MCC AT: 232, MNC  01: A1, 03: TMO, 12: yesss, more

            // get IMSI (EF imsi)

            // get MSISDN (EF msisdn)

            // read/write SMS (EF sms)

            // finally disconnect card
            card.disconnect(false);

            Log.info("waiting for card remove....");
            terminal.waitForCardAbsent(cardWait);
            if (terminal.isCardPresent()) {
                Log.info("card was not removed. exit.");
                return;
            }
            Log.info("card was removed.");
        }
    }

    private static ResponseAPDU processAPDU(CardChannel channel, CommandAPDU apdu, boolean assumeOK) throws Exception {
        Log.debug("request:  " + Hex.toString(apdu.getBytes()));
        ResponseAPDU r = channel.transmit(apdu);
        Log.debug("response: " + Hex.toString((r.getBytes())));
        if (!isOK(r)) {
            String message = "invalid response status: 0x" + Integer.toHexString(r.getSW());
            Log.warn(message);
            if (assumeOK) {
                throw new RuntimeException(message);
            }
        }
        return r;
    }

    private static boolean isOK(ResponseAPDU r) {
        return r.getSW() == 0x9000 || r.getSW1() == 0x61 || r.getSW1() == 0x62 || r.getSW1() == 0x63;
    }
}
