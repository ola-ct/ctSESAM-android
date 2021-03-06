package de.pinyto.ctSESAM;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;

/**
 * Unit tests for the UTF-8 converter.
 */
public class UTF8Test extends TestCase {

    public void testEncode() {
        byte[] expected;
        try {
            expected = "testü".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            assertTrue(false);
            expected = "testü".getBytes();
        }
        byte[] converted = UTF8.encode("testü");
        assertEquals(expected.length, converted.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], converted[i]);
        }
    }

}
