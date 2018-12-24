package com.arellomobile.terminal.helper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * User: AndreyKo
 * Date: 29.05.12
 */
public class MD5Pin
{
    public static byte[] createChecksum(String string) throws Exception
    {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        md.update(string.getBytes(), 0, string.length());
        return md.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Pin(String string) throws Exception
    {
        byte[] b = createChecksum(string);
        String result = "";

        for (int i = 0; i < b.length; i++)
        {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}