package com.leader.api.util;

import org.bson.types.ObjectId;

import javax.servlet.http.HttpSession;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Date;

public class SessionUtil {

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
        KeyPair keyPair = SecureUtil.generateRSAKeyPair(keysize);

        // save private key to session
        savePrivateKeyToSession(session, keyPair.getPrivate());

        return keyPair.getPublic().getEncoded();
    }

    public static String decrypt(HttpSession session, String password, long expire) {
        // get and validate private key
        PrivateKey key = getPrivateKeyFromSession(session, expire);
        if (key == null) {
            return null;
        }

        // decrypt
        return SecureUtil.decryptRSA(password, key);
    }
}
