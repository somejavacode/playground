package test.ufw;


import org.junit.Test;
import ufw.Log;

import java.util.TimeZone;

public class TimeZoneTest {

    @Test
    public void testTZ() {
        Log.info("time zone for XYZ: " + TimeZone.getTimeZone("XYZ"));  // defaults to GMT. should fail IMHO
        Log.info("time zone for UTC: " + TimeZone.getTimeZone("UTC"));
        Log.info("time zone for GMT: " + TimeZone.getTimeZone("GMT"));  // UTC vs GMT: different ID, same "data"
        Log.info("default time zone: " + TimeZone.getDefault());
        // TimeZone.GMT_ID this is not public, why?
    }

}
