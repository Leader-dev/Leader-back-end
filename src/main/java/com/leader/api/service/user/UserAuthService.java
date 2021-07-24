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

    public User createUser(String phone) {
        return createUser(phone, null, null);
    }

    public User createUser(String phone, String password, String nickname) {
        User user = new User();
        user.phone = phone;
        if (password != null) {
            user.password = secureService.encodePassword(password);
        }
        user.nickname = nickname;
        synchronized (userRepository) {
            user.uid = generateNewUid();
            return userRepository.insert(user);
        }
    }

    public boolean hasPassword(String phone) {
        User user = userRepository.findByPhone(phone);
        return user.password != null;
    }

    public boolean validateUser(String phone, String password) {
        User user = userRepository.findByPhone(phone);
        if (user.password == null) {
            return false;
        }
        return secureService.matchesPassword(password, user.password);
    }

    public void updateUserPasswordByPhone(String phone, String newPassword) {
        User user = userRepository.findByPhone(phone);
        user.password = secureService.encodePassword(newPassword);
        userRepository.save(user);
    }

    public void updateUserNicknameByPhone(String phone, String nickname) {
        User user = userRepository.findByPhone(phone);
        user.nickname = nickname;
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
