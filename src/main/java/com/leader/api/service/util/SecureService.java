package com.leader.api.service.util;

import com.leader.api.util.component.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.function.Predicate;

import static com.leader.api.util.ExceptionUtil.ignoreException;

@Service
public class SecureService {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcsdefghijklmnopqrstuvwxyz0123456789";
    private static final String RSA_KEY_PAIR_GENERATOR_ALGORITHM_NAME = "RSA";
    private static final String RSA_CIPHER_TRANSFORMATION_NAME = "RSA/ECB/PKCS1Padding";
    private static final String ARGON2_ALGORITHM_ID = "argon2";

    private static final PasswordEncoder PASSWORD_ENCODER;

    static {
        String idForEncode = ARGON2_ALGORITHM_ID;
        HashMap<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, new Argon2PasswordEncoder());
        PASSWORD_ENCODER = new DelegatingPasswordEncoder(idForEncode, encoders);
    }

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

    public String encodePassword(String message) {
        return PASSWORD_ENCODER.encode(message);
    }

    public boolean matchesPassword(String rawPassword, String password) {
        return PASSWORD_ENCODER.matches(rawPassword, password);
    }

    public String generateRandomNumberId(int length, Predicate<String> regenerate) {
        String generated;
        do {  // ensure that first digit is not 0
            double randomNumber = randomUtil.nextDouble();
            generated = String.valueOf(randomNumber).substring(2, 2 + length);
        } while (generated.startsWith("0") || regenerate.test(generated));
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
