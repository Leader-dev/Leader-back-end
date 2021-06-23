package com.leader.api.service.util;

import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.ClientDataUtil;
import com.leader.api.util.component.DateUtil;
import com.leader.api.util.component.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Service
public class PasswordService {

    public static final String PRIVATE_KEY_ID = "private_key_id";
    public static final String PRIVATE_KEY_TIMESTAMP = "private_key_timestamp";

    private static final int RSA_KEYSIZE = 1024;
    private static final long RSA_KEY_EXPIRE = 60000;

    private final HashMap<UUID, PrivateKey> keyMap = new HashMap<>();

    private final SecureService secureService;
    private final DateUtil dateUtil;
    private final ClientDataUtil clientDataUtil;
    private final RandomUtil randomUtil;

    @Autowired
    public PasswordService(SecureService secureService, DateUtil dateUtil, ClientDataUtil clientDataUtil,
                           RandomUtil randomUtil) {
        this.secureService = secureService;
        this.dateUtil = dateUtil;
        this.clientDataUtil = clientDataUtil;
        this.randomUtil = randomUtil;
    }

    private UUID generateNewUUID() {
        UUID id;
        do {
            id = randomUtil.nextUUID();
        } while (keyMap.containsKey(id));
        return id;
    }

    private UUID putKeyIntoKeyMap(PrivateKey key) {
        UUID id = generateNewUUID();
        keyMap.put(id, key);
        return id;
    }

    private PrivateKey getKeyFromKeyMap(UUID id) {
        return keyMap.get(id);
    }

    public void savePrivateKey(PrivateKey key) {
        // save private to session, recording timestamp
        UUID id = putKeyIntoKeyMap(key);
        clientDataUtil.set(PRIVATE_KEY_ID, id);
        clientDataUtil.set(PRIVATE_KEY_TIMESTAMP, dateUtil.getCurrentDate());
    }

    public PrivateKey getPrivateKey() {
        // get and validate private key
        UUID keyId = clientDataUtil.get(PRIVATE_KEY_ID, UUID.class);
        Date timestamp = clientDataUtil.get(PRIVATE_KEY_TIMESTAMP, Date.class);
        if (keyId == null || timestamp == null) {
            return null;
        }

        // remove private key from session
        clientDataUtil.remove(PRIVATE_KEY_ID);
        clientDataUtil.remove(PRIVATE_KEY_TIMESTAMP);

        // check if key expired
        long timePassed = dateUtil.getCurrentTime() - timestamp.getTime();
        if (timePassed > RSA_KEY_EXPIRE) {
            return null;
        }

        return getKeyFromKeyMap(keyId);
    }

    public byte[] generateKey() {
        KeyPair keyPair = secureService.generateRSAKeyPair(RSA_KEYSIZE);
        savePrivateKey(keyPair.getPrivate());
        return keyPair.getPublic().getEncoded();
    }

    public String decrypt(String password) {
        PrivateKey key = getPrivateKey();
        if (key != null) {
            // decrypt
            String decrypted = secureService.decryptRSA(password, key);
            if (decrypted != null) {
                return decrypted;
            }
        }

        throw new InternalErrorException("Password decryption failed.");
    }
}
