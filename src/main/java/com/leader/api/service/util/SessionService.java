package com.leader.api.service.util;

import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Date;

@Service
public class SessionService {

    public static final String USER_ID = "user_id";

    private final SecureService secureService;

    private final DateUtil dateUtil;

    @Autowired
    public SessionService(SecureService secureService, DateUtil dateUtil) {
        this.secureService = secureService;
        this.dateUtil = dateUtil;
    }

    public void saveUserIdToSession(HttpSession session, ObjectId userid) {
        session.setAttribute(USER_ID, userid);
    }

    public ObjectId getUserIdFromSession(HttpSession session) {
        return (ObjectId) session.getAttribute(USER_ID);
    }

    public void removeUserIdFromSession(HttpSession session) {
        session.removeAttribute(USER_ID);
    }

    public static final String PRIVATE_KEY = "private_key";
    public static final String PRIVATE_KEY_TIMESTAMP = "private_key_timestamp";

    public void savePrivateKeyToSession(HttpSession session, PrivateKey key) {
        // save private to session, recording timestamp
        session.setAttribute(PRIVATE_KEY, key);
        session.setAttribute(PRIVATE_KEY_TIMESTAMP, dateUtil.getCurrentDate());
    }

    public PrivateKey getPrivateKeyFromSession(HttpSession session, long expire) {
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
        long timePassed = dateUtil.getCurrentTime() - timestamp.getTime();
        if (timePassed > expire) {
            return null;
        }

        return key;
    }

    public byte[] generateKey(HttpSession session, int keysize) {
        // generate key
        KeyPair keyPair = secureService.generateRSAKeyPair(keysize);

        // save private key to session
        savePrivateKeyToSession(session, keyPair.getPrivate());

        return keyPair.getPublic().getEncoded();
    }

    public String decrypt(HttpSession session, String password, long expire) {
        // get and validate private key
        PrivateKey key = getPrivateKeyFromSession(session, expire);
        if (key == null) {
            return null;
        }

        // decrypt
        return secureService.decryptRSA(password, key);
    }
}
