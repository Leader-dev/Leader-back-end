package com.leader.api.service.trend;

import com.leader.api.data.trend.puppet.Puppet;
import com.leader.api.data.trend.puppet.PuppetRepository;
import com.leader.api.data.user.UserInfo;
import com.leader.api.service.service.ImageService;
import com.leader.api.service.user.UserInfoService;
import com.leader.api.service.util.UserIdService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PuppetIdService {

    private final UserIdService userIdService;
    private final UserInfoService userInfoService;
    private final PuppetRepository puppetRepository;
    private final ImageService imageService;

    @Autowired
    public PuppetIdService(UserIdService userIdService, UserInfoService userInfoService,
                           PuppetRepository puppetRepository, ImageService imageService) {
        this.userIdService = userIdService;
        this.userInfoService = userInfoService;
        this.puppetRepository = puppetRepository;
        this.imageService = imageService;
    }

    private Puppet createPuppetForUser(ObjectId userId) {
        UserInfo userInfo = userInfoService.getUserInfo(userId);
        Puppet puppet = new Puppet();
        puppet.userId = userId;
        puppet.nickname = userInfo.nickname;
        puppet.avatarUrl = imageService.duplicateImage(userInfo.avatarUrl);
        puppetRepository.insert(puppet);
        return puppet;
    }

    public ObjectId getCurrentUserId() {
        return userIdService.getCurrentUserId();
    }

    public ObjectId getPuppetId(ObjectId userId) {
        Puppet puppet = puppetRepository.findByUserId(userId);
        if (puppet == null) {
            puppet = createPuppetForUser(userId);
        }
        return puppet.id;
    }

    public ObjectId getUserId(ObjectId puppetId) {
        Puppet puppet = puppetRepository.findById(puppetId, Puppet.class);
        return puppet.userId;
    }

    public ObjectId getCurrentPuppetId() {
        ObjectId userId = getCurrentUserId();
        return getPuppetId(userId);
    }
}
