package org.backmeup.storage.model.utils;

import java.io.UnsupportedEncodingException;

// taken from http://www.rgagnon.com/javadetails/java-0596.html
public class StringUtils {

    static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2',
        (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c',
        (byte) 'd', (byte) 'e', (byte) 'f' };

    private StringUtils() {
    }

    public static String getHexString(byte[] raw)
            throws UnsupportedEncodingException {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }
}
