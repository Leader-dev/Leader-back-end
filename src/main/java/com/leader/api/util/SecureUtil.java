package com.leader.api.util;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class SecureUtil {

    private final static String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcsdefghijklmnopqrstuvwxyz0123456789";
    private final static String SHA1_MESSAGE_DIGEST_ALGORITHM_NAME = "SHA-1";
    private final static String RSA_KEY_PAIR_GENERATOR_ALGORITHM_NAME = "RSA";
    private final static String RSA_CIPHER_TRANSFORMATION_NAME = "RSA/ECB/PKCS1Padding";

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
            MessageDigest messageDigest = MessageDigest.getInstance(SHA1_MESSAGE_DIGEST_ALGORITHM_NAME);
            messageDigest.update(message.getBytes());
            return new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateRandomUid(int length) {
        SecureRandom random = new SecureRandom();
        double randomNumber = random.nextDouble();
        String generated;
        do {  // ensure that first digit is not 0
            generated = String.valueOf(randomNumber).substring(2, 2 + length);
        } while (generated.startsWith("0"));
        return generated;
    }

    public static String generateRandomAuthCode(int length) {
        SecureRandom random = new SecureRandom();
        double randomNumber = random.nextDouble();
        return String.valueOf(randomNumber).substring(2, 2 + length);
    }

    public static KeyPair generateRSAKeyPair(int keysize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_KEY_PAIR_GENERATOR_ALGORITHM_NAME);
            generator.initialize(keysize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new KeyPair(null, null);
        }
    }

    public static String decryptRSA(String cipherText, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORMATION_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytesIn = cipherText.getBytes(StandardCharsets.UTF_8);
            byte[] bytesDecoded = Base64.getDecoder().decode(bytesIn);
            return new String(cipher.doFinal(bytesDecoded));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
