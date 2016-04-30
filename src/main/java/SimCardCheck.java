import ufw.Hex;
import ufw.Log;
import ufw.Validate;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

        int termNr = 0;
        if (terminals.size() > 1) {
            Log.info("found " + terminals.size() + " terminals:");
            for (int i = 0; i < terminals.size(); i++) {
                Log.info(i + ": " + terminals.get(i).getName());
            }
            System.out.print("Please select number: ");
            String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
            termNr = Integer.parseInt(input);
        }

        CardTerminal terminal = terminals.get(termNr);
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

            // 3GPP TS 11.11 http://www.3gpp.org/dynareport/1111.htm
            // -> http://www.etsi.org/deliver/etsi_ts/100900_100999/100977/08.14.00_60/ts_100977v081400p.pdf
            // 9  Description of the commands
            // 10 Contents of the Elementary Files (EF)
            // select MF
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("3F00"), 0x00), false);
            // 9F16  = 16 bytes... SW2 .. length
            Validate.isTrue(r.getSW1() == 0x9F);
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xC0, 0x00, 0x00, r.getSW2()), false);
            Log.info("MF content: " + Hex.toString(r.getData()));

            // Y1: 0000955A3F00010000AAFA010DB304060500838A838A00008383
            // H1: 00004F583F0001000000000013B303090400838A838A000300004F5800004F58

            // select ICCID (MF level)
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("2FE2"), 0x00), false);
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xC0, 0x00, 0x00, r.getSW2()), false);
            Log.info("select ICCID response: " + Hex.toString(r.getData()));
            // file size
            byte size = r.getData()[3];  // knowing, will be 0x0A bytes (Note: code assumes < 256)
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xB0, 0x00, 0x00, size), false);
            Log.info("ICCID content: " + Hex.toString(r.getData()));

            // Y1: 983421902021033974F3,
            //     8943120902123093473 matches number printed on card
            // H1: 98347000004120433998
            //     89430700001402349389 number on card 1402349389

            // select "MF"/"GSM"
            processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("2FE2"), 0x00), false);
            processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("7F20"), 0x00), false);
            // select IMSI (GSM level)
            processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("6F07"), 0x00), false);
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xC0, 0x00, 0x00, r.getSW2()), false);
            Log.info("select IMSI response: " + Hex.toString(r.getData()));
            // file size
            size = r.getData()[3];
            r = processAPDU(channel, new CommandAPDU(0xA0, 0xB0, 0x00, 0x00, size), false);
            Log.info("IMSI content: " + Hex.toString(r.getData()));

            // Y1: 082923212802905417
            //     909232128220094571 imsi: 232128220094571, MCC 12 = Yesss

            // H1: 082923706701906883
            //     809232077610098638 imsi: 232077610098638, MCC 07 = tele.ring? T-Mobile Austria

            // https://en.wikipedia.org/wiki/Mobile_country_code MCC AT: 232, MNC  01: A1, 03: TMO, 12: yesss, more


            // select MF/"Telecom"
//            processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("3F00"), 0x00), false);
//            processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("7F10"), 0x00), false);
//            // get MSISDN (Telecom level)
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xA4, 0x00, 0x00, Hex.fromString("6F40"), 0x00), false);
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xC0, 0x00, 0x00, r.getSW2()), false);
//            Log.info("select MSISDN response: " + Hex.toString(r.getData()));
//            // file size
//            size = r.getData()[3];
//            r = processAPDU(channel, new CommandAPDU(0xA0, 0xB0, 0x00, 0x00, size), false);
//            Log.info("MSISDN content: " + Hex.toString(r.getData()));



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
