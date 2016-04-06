import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DLSet;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SmartCardCheck {

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
            card = terminal.connect("*");

            Log.info("card: " + card); // not that much info in toString(). reader, protocol, state

            // list of ATRs: http://ludovic.rousseau.free.fr/softwares/pcsc-tools/smartcard_list.txt
            byte[] atr = card.getATR().getBytes();
            Log.info("card ATR bytes=" + Hex.toString(atr));
            // "historical bytes" are contained in "bytes"
            // Log.info("card ATR historical bytes=" + Hex.toString(card.getATR().getHistoricalBytes()));

            // Austrian "e-Card" special Version of Starcos 3.1
            byte[] sv1 = Hex.fromString("3BBD18008131FE45805102670414B10101020081053D");
            // Austrian "e-Card" of the 4th generation.
            byte[] sv2 = Hex.fromString("3BDF18008131FE588031B05202046405C903AC73B7B1D422");
            // Austrian "e-card" G3 (running StarCOS 3.4 by Giesecke & Devrient)
            byte[] sv3 = Hex.fromString("3BDD96FF81B1FE451F038031B052020364041BB422810518");

            Log.setLevel(Log.Level.INFO);  // set DEBUG to get all detail data (PDUs, full ASN1)

            if (Arrays.equals(atr, sv1) || Arrays.equals(atr, sv2) || Arrays.equals(atr, sv3)) {
                Log.info("got e-card. try to read some data.");
                CardChannel channel = card.getBasicChannel();

                // try some APDUs. see: http://demo.a-sit.at/smart-card-applet/
                processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x04, 0x00, Hex.fromString("D040000017010101"), 0xff));
                processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x02, 0x04, Hex.fromString("EF01"), 0xff));
                ResponseAPDU resp = processAPDU(channel, new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 0xff));

                byte[] data = resp.getData();
                card.disconnect(false);
                Log.debug("asn1 data from card: " + Hex.toString(data));
                ASN1InputStream ais = new ASN1InputStream(new ByteArrayInputStream(data));
                // original code used "while", but fails for old e-card with "java.io.IOException: unexpected end-of-contents marker"
                // all data returned with first "readObject()"
                if (ais.available() > 0) {
                    ASN1Primitive obj = ais.readObject();
                    Log.debug("asn1 content decoded:\n" + ASN1Dump.dumpAsString(obj, true));
                    Log.info("surname=     " + getByOid(obj, "2.5.4.4"));
                    Log.info("givenName=   " + getByOid(obj, "2.5.4.42"));
                    Log.info("dateOfBirth= " + getByOid(obj, "1.3.6.1.5.5.7.9.1"));
                    Log.info("svNr=        " + getByOid(obj, "1.2.40.0.10.1.4.1.1"));
                }
                ais.close();
            }
            Log.info("waiting for card remove....");
            terminal.waitForCardAbsent(cardWait);
            if (terminal.isCardPresent()) {
                Log.info("card was not removed. exit.");
                return;
            }
            Log.info("card was removed.");
        }
    }

    private static Object getByOid(ASN1Primitive obj, String oidMatch) throws Exception {
        if (obj instanceof ASN1Sequence) {
            // this code is rather "optimistic" when it comes to casting...
            Iterator<ASN1Encodable> iterator = ((ASN1Sequence) obj).iterator();
            while (iterator.hasNext()) {
                ASN1Encodable encodable = iterator.next();  // knowing...
                Iterator<ASN1Encodable> iterator2 = ((ASN1Sequence) encodable).iterator();
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) iterator2.next();
                if (oid.getId().equals(oidMatch)) {
                    DLSet set = (DLSet) iterator2.next();
                    ASN1Encodable value = set.iterator().next();
                    if (value instanceof ASN1GeneralizedTime) { // fix missing toString
                        return ((ASN1GeneralizedTime) value).getDate();
                    }
                    return value;
                }
            }
            return "not found";
        }
        else return "not found";
    }

    private static ResponseAPDU processAPDU(CardChannel channel, CommandAPDU apdu) throws Exception {
        Log.debug("request:  " + Hex.toString(apdu.getBytes()));
        ResponseAPDU r = channel.transmit(apdu);
        Log.debug("response: " + Hex.toString((r.getBytes())));
        if (r.getSW() == 0x9000 || r.getSW1() == 0x61 || r.getSW1() == 0x62 || r.getSW1() == 0x63) {
            return r;
        }
        throw new RuntimeException("invalid response status: " + r.getSW());
    }
}
