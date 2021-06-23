package com.leader.api.service.util;

import com.leader.api.util.component.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static com.leader.api.util.ExceptionUtil.ignoreException;

@Service
public class SecureService {

    private final static String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcsdefghijklmnopqrstuvwxyz0123456789";
    private final static String SHA1_MESSAGE_DIGEST_ALGORITHM_NAME = "SHA-1";
    private final static String RSA_KEY_PAIR_GENERATOR_ALGORITHM_NAME = "RSA";
    private final static String RSA_CIPHER_TRANSFORMATION_NAME = "RSA/ECB/PKCS1Padding";

    private final RandomUtil randomUtil;

    @Autowired
    public SecureService(RandomUtil randomUtil) {
        this.randomUtil = randomUtil;
    }

    public String generateRandomSalt(int length) {
        StringBuilder saltBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = randomUtil.nextInt(CHARSET.length());
            char ch = CHARSET.charAt(randomIndex);
            saltBuilder.append(ch);
        }
        return saltBuilder.toString();
    }

    public String SHA1(String message) {
        return ignoreException(() -> {
            // apply SHA-1
            MessageDigest messageDigest = MessageDigest.getInstance(SHA1_MESSAGE_DIGEST_ALGORITHM_NAME);
            messageDigest.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = messageDigest.digest();

            // convert byte array to hex array
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return new String(builder);
        });
    }

    public String generateRandomNumberId(int length) {
        String generated;
        do {  // ensure that first digit is not 0
            double randomNumber = randomUtil.nextDouble();
            generated = String.valueOf(randomNumber).substring(2, 2 + length);
        } while (generated.startsWith("0"));
        return generated;
    }

    public String generateRandomAuthCode(int length) {
        double randomNumber = randomUtil.nextDouble();
        return String.valueOf(randomNumber).substring(2, 2 + length);
    }

    public KeyPair generateRSAKeyPair(int keysize) {
        return ignoreException(() -> {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_KEY_PAIR_GENERATOR_ALGORITHM_NAME);
            generator.initialize(keysize);
            return generator.generateKeyPair();
        });
    }

    public String encryptRSA(String plainText, PublicKey key) {
        return ignoreException(() -> {
            Cipher cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORMATION_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytesIn = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] bytesEncrypted = cipher.doFinal(bytesIn);
            return Base64.getEncoder().encodeToString(bytesEncrypted);
        });
    }

    public String decryptRSA(String cipherText, PrivateKey key) {
        return ignoreException(() -> {
            Cipher cipher = Cipher.getInstance(RSA_CIPHER_TRANSFORMATION_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytesIn = cipherText.getBytes(StandardCharsets.UTF_8);
            byte[] bytesDecoded = Base64.getDecoder().decode(bytesIn);
            return new String(cipher.doFinal(bytesDecoded));
        });
    }
}
