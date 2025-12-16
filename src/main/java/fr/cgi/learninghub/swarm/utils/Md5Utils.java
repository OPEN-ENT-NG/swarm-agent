package fr.cgi.learninghub.swarm.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String OUTPUT_FORMAT = "%-20s:%s";

    public static String encrypt(String input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        byte[] result = md.digest(input.getBytes());
        return new String(result);
    }

}
