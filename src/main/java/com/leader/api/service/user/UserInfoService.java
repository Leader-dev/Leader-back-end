package com.leader.api.service.user;

import com.leader.api.data.user.UserInfo;
import com.leader.api.data.user.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {

    private final UserRepository userRepository;

    @Autowired
    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfo getUserInfo(ObjectId id) {
        return userRepository.findUserById(id, UserInfo.class);
    }

    public String getUserNickname(ObjectId id) {
        return getUserInfo(id).nickname;
    }

    public String getPortrait(ObjectId id) {
        return getUserInfo(id).portraitUrl;
    }

    public void updateNickname(ObjectId id, String nickname) {
        userRepository.findById(id).ifPresent(user -> {
            user.nickname = nickname;
            userRepository.save(user);
        });
    }

    public void updatePortrait(ObjectId id, String portraitUrl) {
        userRepository.findById(id).ifPresent(user -> {
            user.portraitUrl = portraitUrl;
            userRepository.save(user);
        });
    }
}
