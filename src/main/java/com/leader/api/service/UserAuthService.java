package com.leader.api.service;

import com.leader.api.data.user.AuthCodeRecord;
import com.leader.api.data.user.AuthCodeRecordRepository;
import com.leader.api.data.user.User;
import com.leader.api.data.user.UserRepository;
import com.leader.api.util.SecureUtil;
import com.leader.api.util.SessionUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Service
public class UserAuthService {

    private static final int RSA_KEYSIZE = 1024;
    private static final long RSA_KEY_EXPIRE = 60000;
    private static final int AUTHCODE_LENGTH = 6;
    private static final long AUTHCODE_REQUEST_INTERVAL = 60000;
    private static final long AUTHCODE_EXPIRE = 300000;
    private static final int SALT_LENGTH = 16;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthCodeRecordRepository authCodeRecordRepository;

    private long timePassedSinceLastAuthCode(String phone) {
        AuthCodeRecord authCodeRecord = authCodeRecordRepository.findByPhone(phone);
        if (authCodeRecord == null) {
            return -1;
        }
        return new Date().getTime() - authCodeRecord.timestamp.getTime();
    }

    private void insertAuthCodeRecord(String phone, String authcode) {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.phone = phone;
        authCodeRecord.authcode = authcode;
        authCodeRecord.timestamp = new Date();
        authCodeRecordRepository.deleteByPhone(phone);  // make sure previous ones are deleted
        authCodeRecordRepository.insert(authCodeRecord);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public void assertUsernameExists(String username) {
        if (!usernameExists(username)) {
            throw new RuntimeException("Username not exist");
        }
    }

    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public void assertPhoneExists(String phone) {
        if (!phoneExists(phone)) {
            throw new RuntimeException("Phone not exist");
        }
    }

    public byte[] generateKeyPair(HttpSession session) {
        return SessionUtil.generateKey(session, RSA_KEYSIZE);
    }

    public String decryptPassword(HttpSession session, String password) {
        String decryptedPassword = SessionUtil.decrypt(session, password, RSA_KEY_EXPIRE);
        if (decryptedPassword == null) {
            throw new RuntimeException("Key invalid");
        }
        return decryptedPassword;
    }

    public boolean sendAuthCode(String phone) {
        long timePassed = timePassedSinceLastAuthCode(phone);
        if (timePassed != -1 && timePassed < AUTHCODE_REQUEST_INTERVAL) {
            return false;
        }

        // randomly generate authcode
        String authcode = SecureUtil.generateRandomAuthCode(AUTHCODE_LENGTH);

        // insert record to database
        insertAuthCodeRecord(phone, authcode);

        // TODO Actually send the authcode to phone

        return true;
    }

    public boolean validateAuthCode(String phone, String authcode) {
        AuthCodeRecord authCodeRecord = authCodeRecordRepository.findByPhone(phone);
        if (authCodeRecord == null) {
            return false;
        }
        long timePassed = new Date().getTime() - authCodeRecord.timestamp.getTime();
        return timePassed <= AUTHCODE_EXPIRE && authCodeRecord.authcode.equals(authcode);
    }

    public void removeAuthCodeRecord(String phone) {
        authCodeRecordRepository.deleteByPhone(phone);
    }

    public void createUser(String username, String password, String phone) {
        User user = new User();
        user.username = username;
        user.phone = phone;
        String salt = SecureUtil.createRandomSalt(SALT_LENGTH);
        user.password = SecureUtil.SHA1(password + salt);
        user.salt = salt;
        userRepository.insert(user);
    }

    public boolean validateUser(String username, String password) {
        assertUsernameExists(username);
        User user = userRepository.findByUsername(username);
        String salt = user.salt;
        String processedPassword = SecureUtil.SHA1(password + salt);
        return user.password.equals(processedPassword);
    }

    public void updateUserPasswordByPhone(String phone, String newPassword) {
        assertPhoneExists(phone);
        User user = userRepository.findByPhone(phone);
        String salt = SecureUtil.createRandomSalt(SALT_LENGTH);
        user.password = SecureUtil.SHA1(newPassword + salt);
        user.salt = salt;
        userRepository.save(user);
    }

    public ObjectId getUserIdByUsername(String username) {
        assertUsernameExists(username);
        User user = userRepository.findByUsername(username);
        return user.id;
    }
}
