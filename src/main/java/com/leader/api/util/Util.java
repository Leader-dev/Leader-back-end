package com.leader.api.util;

import org.bson.types.ObjectId;

import javax.crypto.*;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Date;

public class Util {

    private final static String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcsdefghijklmnopqrstuvwxyz0123456789";

    public static String createRandomSalt(int length) {
        StringBuilder saltBuilder = new StringBuilder(length);
        SecureRandom random = new SecureRandom();  // unpredictable random number generator
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARSET.length());
            char ch = CHARSET.charAt(randomIndex);
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
            return null;
        }
    }

    public static KeyPair generateRSAKeyPair(int keysize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keysize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new KeyPair(null, null);
        }
    }

    public static String decryptRSA(String cipherText, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytesIn = cipherText.getBytes(StandardCharsets.UTF_8);
            byte[] bytesDecoded = Base64.getDecoder().decode(bytesIn);
            return new String(cipher.doFinal(bytesDecoded));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String USER_ID = "user_id";

    public static void saveUserIdToSession(HttpSession session, ObjectId userid) {
        session.setAttribute(USER_ID, userid);
    }

    public static ObjectId getUserIdFromSession(HttpSession session) {
        return (ObjectId) session.getAttribute(USER_ID);
    }

    private static final String PRIVATE_KEY = "private_key";
    private static final String PRIVATE_KEY_TIMESTAMP = "private_key_timestamp";

    public static void  savePrivateKeyToSession(HttpSession session, PrivateKey key) {
        // save private to session, recording timestamp
        session.setAttribute(PRIVATE_KEY, key);
        session.setAttribute(PRIVATE_KEY_TIMESTAMP, new Date());
    }

    public static PrivateKey getPrivateKeyFromSession(HttpSession session, long expire) {
        // get and validate private key
        PrivateKey key = (PrivateKey) session.getAttribute(PRIVATE_KEY);
        Date timestamp = (Date) session.getAttribute(PRIVATE_KEY_TIMESTAMP);
        if (key == null || timestamp == null) {
            return null;
        }

        // remove private key from session
        session.removeAttribute(PRIVATE_KEY);
        session.removeAttribute(PRIVATE_KEY_TIMESTAMP);

        // check if key expired
        long timePassed =  new Date().getTime() - timestamp.getTime();
        if (timePassed > expire) {
            return null;
        }

        return key;
    }

    public static byte[] generateKey(HttpSession session, int keysize) {
        // generate key
        KeyPair keyPair = Util.generateRSAKeyPair(keysize);

        // save private key to session
        Util.savePrivateKeyToSession(session, keyPair.getPrivate());

        return keyPair.getPublic().getEncoded();
    }

    public static String decrypt(HttpSession session, String password, long expire) {
        // get and validate private key
        PrivateKey key = Util.getPrivateKeyFromSession(session, expire);
        if (key == null) {
            return null;
        }

        // decrypt
        return Util.decryptRSA(password, key);
    }
}
