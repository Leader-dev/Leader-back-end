package com.leader.api.service.user;

import com.leader.api.data.user.User;
import com.leader.api.data.user.UserRepository;
import com.leader.api.service.util.SecureService;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

    private static final int UID_LENGTH = 8;
    private static final long UID_LENGTH_CAPACITY = 50000000;
    private static final int SALT_LENGTH = 16;

    private final UserRepository userRepository;
    private final SecureService secureService;

    @Autowired
    public UserAuthService(UserRepository userRepository, SecureService secureService) {
        this.userRepository = userRepository;
        this.secureService = secureService;
    }

    private String generateNewUid() {
        if (userRepository.count() > UID_LENGTH_CAPACITY) {
            throw new InternalErrorException("Uid length capacity exceeded");
        }
        return secureService.generateRandomNumberId(
                UID_LENGTH,
                this::uidExists
        );
    }

    public boolean uidExists(String uid) {
        return userRepository.existsByUid(uid);
    }

    public void assertUidExists(String uid) {
        if (!uidExists(uid)) {
            throw new InternalErrorException("Uid not exist");
        }
    }

    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public void assertPhoneExists(String phone) {
        if (!phoneExists(phone)) {
            throw new InternalErrorException("Phone not exist");
        }
    }

    public User createUser(String phone, String password, String nickname) {
        User user = new User();
        user.phone = phone;
        String salt = secureService.generateRandomSalt(SALT_LENGTH);
        user.password = secureService.SHA1(password + salt);
        user.salt = salt;
        user.nickname = nickname;
        synchronized (userRepository) {
            user.uid = generateNewUid();
            return userRepository.insert(user);
        }
    }

    public boolean validateUser(String phone, String password) {
        assertPhoneExists(phone);
        User user = userRepository.findByPhone(phone);
        String salt = user.salt;
        String processedPassword = secureService.SHA1(password + salt);
        return user.password.equals(processedPassword);
    }

    public void updateUserPasswordByPhone(String phone, String newPassword) {
        assertPhoneExists(phone);
        User user = userRepository.findByPhone(phone);
        String salt = secureService.generateRandomSalt(SALT_LENGTH);
        user.password = secureService.SHA1(newPassword + salt);
        user.salt = salt;
        userRepository.save(user);
    }

    public ObjectId getUserIdByPhone(String phone) {
        assertPhoneExists(phone);
        User user = userRepository.findByPhone(phone);
        return user.id;
    }

    public String getUserPhoneById(ObjectId id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        return user.phone;
    }
}
