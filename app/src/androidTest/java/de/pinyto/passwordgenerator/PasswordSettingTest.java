package de.pinyto.passwordgenerator;

import junit.framework.TestCase;

/**
 * Testing the setting container class.
 */
public class PasswordSettingTest extends TestCase {

    public void testUsername() {
        PasswordSetting s = new PasswordSetting("unit.test");
        assertEquals("", s.getUsername());
        s.setUsername("Hugo");
        assertEquals("Hugo", s.getUsername());
    }

    public void testLegacyPassword() {
        PasswordSetting s = new PasswordSetting("unit.test");
        assertEquals("", s.getLegacyPassword());
        s.setLegacyPassword("K6x/vyG9(p");
        assertEquals("K6x/vyG9(p", s.getLegacyPassword());
    }

    public void testCharacterSet() {
        PasswordSetting s = new PasswordSetting("unit.test");
        assertFalse(s.useCustomCharacterSet());
        assertEquals("", s.getCustomCharacterSet());
        s.setCustomCharacterSet("&=Oo0wWsS$#uUvVzZ");
        assertTrue(s.useCustomCharacterSet());
        assertEquals("&=Oo0wWsS$#uUvVzZ", s.getCustomCharacterSet());
        s.setCustomCharacterSet(
                "abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHJKLMNPQRTUVWXYZ" +
                "0123456789" +
                "#!\"§$%&/()[]{}=-_+*<>;:.");
        assertFalse(s.useCustomCharacterSet());
        assertEquals("", s.getCustomCharacterSet());
    }

    public void testGetDefaultCharacterSet() {
        PasswordSetting s = new PasswordSetting("unit.test");
        s.setUseLetters(false);
        assertEquals("0123456789#!\"§$%&/()[]{}=-_+*<>;:.", s.getDefaultCharacterSet());
        s.setUseLetters(true);
        s.setUseDigits(false);
        s.setUseExtra(false);
        assertEquals("abcdefghijklmnopqrstuvwxyzABCDEFGHJKLMNPQRTUVWXYZ",
                s.getDefaultCharacterSet());
    }

    public void testGetCharacterSet() {
        PasswordSetting s = new PasswordSetting("unit.test");
        assertEquals("c", s.getCharacterSet().get(2));
        s.setCustomCharacterSet("axFLp0");
        assertEquals(6, s.getCharacterSet().size());
        assertEquals("F", s.getCharacterSet().get(2));
        assertEquals("0", s.getCharacterSet().get(5));
    }

    public void testSetCreationDate() {
        PasswordSetting s = new PasswordSetting("unit.test");
        s.setModificationDate("1995-01-01T01:14:12");
        s.setCreationDate("2001-01-01T02:14:12");
        assertEquals("2001-01-01T02:14:12", s.getCreationDate());
        assertEquals("2001-01-01T02:14:12", s.getModificationDate());
    }

    public void testSetModificationDate() {
        PasswordSetting s = new PasswordSetting("unit.test");
        s.setCreationDate("2007-01-01T02:14:12");
        s.setModificationDate("2005-01-01T01:14:12");
        assertEquals("2005-01-01T01:14:12", s.getCreationDate());
        assertEquals("2005-01-01T01:14:12", s.getModificationDate());
    }

}
