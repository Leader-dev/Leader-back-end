package com.leader.api.service.user;

import com.leader.api.data.user.User;
import com.leader.api.data.user.UserInfo;
import com.leader.api.data.user.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class UserInfoService {

    private final UserRepository userRepository;

    @Autowired
    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void operateAndSaveUser(ObjectId id, Consumer<User> consumer) {
        userRepository.findById(id).ifPresent(user -> {
            consumer.accept(user);
            userRepository.save(user);
        });
    }

    public UserInfo getUserInfo(ObjectId id) {
        return userRepository.findById(id, UserInfo.class);
    }

    public UserInfo getUserInfo(String uid) {
        return userRepository.findByUid(uid, UserInfo.class);
    }

    public String getUserNickname(ObjectId id) {
        return getUserInfo(id).nickname;
    }

    public String getAvatar(ObjectId id) {
        return getUserInfo(id).avatarUrl;
    }

    public void updateNickname(ObjectId id, String nickname) {
        operateAndSaveUser(id, user -> {
            user.nickname = nickname;
        });
    }

    public void updateAvatar(ObjectId id, String avatarUrl) {
        operateAndSaveUser(id, user -> {
            user.avatarUrl = avatarUrl;
        });
    }
}
