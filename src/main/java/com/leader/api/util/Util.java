package com.leader.api.util;

import org.bson.types.ObjectId;

import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Util {

    private final static String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcsdefghijklmnopqrstuvwxyz0123456789";

    public static String createRandomSalt(int length) {
        final StringBuilder saltBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final int randomIndex = new Random().nextInt(CHARSET.length());
            final char ch = CHARSET.charAt(randomIndex);
            saltBuilder.append(ch);
        }
        return saltBuilder.toString();
    }

    public static String SHA1(String message) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(message.getBytes());
            return new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static ObjectId getUserIdFromSession(HttpSession session) {
        return (ObjectId) session.getAttribute("user_id");
    }
}
