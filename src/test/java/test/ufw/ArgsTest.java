package test.ufw;

import org.junit.Assert;
import org.junit.Test;
import ufw.Args;
import ufw.Log;

public class ArgsTest {

    private static final String PRE = Args.OPTION_PREFIX;
    private static final String PRE_FLAG = Args.FLAG_PREFIX;
    private static final String NAME1 = "name1";
    private static final String NAME2 = "name2";
    private static final String NAME3 = "name3";
    private static final int INT_VALUE1 = 1234;
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String EXTRA1 = "extra1";
    private static final String FLAG1 = "flag1";
    private static final String FLAG2 = "flag2";
    private static final String FLAG3 = "flag3";
    private static final int INT_EXTRA1 = 4321;
    private static final String EXTRA2 = "extra2";

    @Test
    public void testEmptyArgs() {
        String[] testArgs = {};
        Args args = new Args(testArgs);
        Assert.assertNull(args.getExtraValue(0));
        Assert.assertTrue(args.getExtraIntValue(0) == 0);
        Assert.assertNull(args.getValue("test"));
        Assert.assertTrue(args.getIntValue("test") == 0);
    }

    @Test
    public void testArgs() {
        String[] testArgs = {PRE + NAME1, VALUE1, PRE + NAME2 , VALUE2};
        Args args = new Args(testArgs);
        Assert.assertNull(args.getExtraValue(0));
        Assert.assertEquals(VALUE1, args.getValue(NAME1));
        Assert.assertEquals(VALUE2, args.getValue(NAME2));
        Assert.assertNull(args.getValue(NAME3));
        Assert.assertEquals(VALUE2, args.getValue(NAME3, VALUE2));
    }

    @Test
    public void testIntArgs() {
        String[] testArgs = {PRE + NAME1, Integer.toString(INT_VALUE1), Integer.toString(INT_EXTRA1)};
        Args args = new Args(testArgs);
        Assert.assertEquals(INT_VALUE1, args.getIntValue(NAME1));
        Assert.assertEquals(INT_EXTRA1, args.getExtraIntValue(0));

        Assert.assertEquals(0, args.getIntValue(NAME2));
        Assert.assertEquals(333, args.getIntValue(NAME2, 333));
        Assert.assertEquals(0, args.getExtraIntValue(1));
    }

    @Test
    public void testInvalidIntArgs() {
        String[] testArgs = {PRE + NAME1, VALUE1, EXTRA1};
        Args args = new Args(testArgs);
        try {
            args.getIntValue(NAME1);
            Assert.fail();
        }
        catch (NumberFormatException nfe) {
            Log.info("expected: " + nfe);
        }
        try {
            args.getExtraIntValue(0);
            Assert.fail();
        }
        catch (NumberFormatException nfe) {
            Log.info("expected: " + nfe);
        }
    }

    @Test
    public void testWithExtraArgs() {
        String[] testArgs = {PRE + NAME1, VALUE1, PRE + NAME2 , VALUE2, EXTRA1, EXTRA2};
        Args args = new Args(testArgs);
        Assert.assertEquals(VALUE1, args.getValue(NAME1));
        Assert.assertEquals(VALUE2, args.getValue(NAME2));
        Assert.assertNull(args.getValue(NAME3));

        Assert.assertEquals(EXTRA1, args.getExtraValue(0));
        Assert.assertEquals(EXTRA2, args.getExtraValue(1));
        Assert.assertNull(args.getExtraValue(2));
    }

    @Test
    public void testWithFlags() {
        String[] testArgs = {PRE_FLAG + FLAG2, PRE + NAME1, VALUE1, PRE + NAME2 , VALUE2, PRE_FLAG + FLAG1};
        Args args = new Args(testArgs);
        Assert.assertEquals(VALUE1, args.getValue(NAME1));
        Assert.assertEquals(VALUE2, args.getValue(NAME2));
        Assert.assertNull(args.getValue(NAME3));
        Assert.assertEquals(VALUE2, args.getValue(NAME3, VALUE2));
        Assert.assertEquals(INT_VALUE1, args.getIntValue(NAME3, INT_VALUE1));

        Assert.assertTrue(args.hasFlag(FLAG1));
        Assert.assertTrue(args.hasFlag(FLAG2));
        Assert.assertFalse(args.hasFlag(FLAG3));
    }

    @Test
    public void testFlags() {
        String[] testArgs = {PRE_FLAG + FLAG1, PRE_FLAG + FLAG2};
        Args args = new Args(testArgs);
        Assert.assertTrue(args.hasFlag(FLAG1));
        Assert.assertTrue(args.hasFlag(FLAG2));
        Assert.assertFalse(args.hasFlag(FLAG3));
    }

    @Test
    public void testOnlyExtraArgs() {
        String[] testArgs = {EXTRA1, EXTRA2};
        Args args = new Args(testArgs);
        Assert.assertNull(args.getValue(NAME1));

        Assert.assertEquals(EXTRA1, args.getExtraValue(0));
        Assert.assertEquals(EXTRA2, args.getExtraValue(1));
        Assert.assertNull(args.getExtraValue(2));
    }

    @Test
    public void testFail() {
        assumeFail(new String[] {EXTRA1, PRE + EXTRA2});
        assumeFail(new String[] {PRE + NAME1, PRE + NAME2});
        assumeFail(new String[] {PRE + NAME1, PRE_FLAG + FLAG1});
    }

    private void assumeFail(String[] args) {
        try {
            new Args(args);
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected: " + e);
        }
    }

}
