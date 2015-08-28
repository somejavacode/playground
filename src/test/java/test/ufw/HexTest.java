package test.ufw;

import org.junit.Assert;
import org.junit.Test;
import ufw.Hex;
import ufw.Log;

public class HexTest {

    @Test
    public void testConvert() {
        // encode to String
        byte[] array = new byte[] {0x00, (byte) 0xff, 0x33};
        String hexStr = Hex.toString(array);
        Assert.assertEquals("00FF33", hexStr);
        String hexStr2 = Hex.toString(array, 1, 2);
        Assert.assertEquals("FF33", hexStr2);
        // decode from String
        byte[] arrayBack = Hex.fromString(hexStr);
        Assert.assertArrayEquals(array, arrayBack);
    }

    @Test
    public void testErrorsFromString() {
        try {
            Hex.fromString("AAABA");  // odd number of characters
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception: " + e);
        }
        try {
            Hex.fromString("AABBCCGG");  // bad characters
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception: " + e);
        }
    }

    @Test
    public void testErrorsToString() {
        try {
            Hex.toString(new byte[]{22, 22}, 0, 3);
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception: " + e);
        }
        try {
            Hex.toString(new byte[]{22, 22}, -1, 2);
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception: " + e);
        }
    }

    @Test
    public void testToStringBlock() throws Exception {
        String eight1 = "12345678";
        String eight2 = "abcdefgh";
        byte[] data = (eight1 + eight2 + eight1).getBytes();
        Log.info("result0:\n" + Hex.toStringBlock(data));

        Log.info("result1:\n" + Hex.toStringBlock(data, 16, 8, false));
        Log.info("result2:\n" + Hex.toStringBlock(data, 12, 4, true));
        Log.info("result3:\n" + Hex.toStringBlock(data, 16, 5, true));
        Log.info("result4:\n" + Hex.toStringBlock(data, 16, 4, true));

        Log.info("result5:\n" + Hex.toStringBlock(new byte[] {0, 1, 2, 3, 13, 10}, 16, 8, true));

        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte)i;
        }
        Log.info("result8:\n" + Hex.toStringBlock(allBytes, 16, 8, true));

        for (int i = 14; i < 32; i++) {
            byte[] bytes2 = new byte[i];
            System.arraycopy(allBytes, 0, bytes2, 0, i);
            Log.info("result16/4:\n" + Hex.toStringBlock(bytes2, 16, 4, true));
            Log.info("result16/3:\n" + Hex.toStringBlock(bytes2, 16, 3, true));
        }
    }
}
