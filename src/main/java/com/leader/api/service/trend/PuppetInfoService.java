package com.leader.api.service.trend;

import com.leader.api.data.trend.puppet.Puppet;
import com.leader.api.data.trend.puppet.PuppetInfo;
import com.leader.api.data.trend.puppet.PuppetRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class PuppetInfoService {

    private final PuppetRepository puppetRepository;

    @Autowired
    public PuppetInfoService(PuppetRepository puppetRepository) {
        this.puppetRepository = puppetRepository;
    }

    private void operateAndSavePuppet(ObjectId id, Consumer<Puppet> consumer) {
        puppetRepository.findById(id).ifPresent(puppet -> {
            consumer.accept(puppet);
            puppetRepository.save(puppet);
        });
    }

    public PuppetInfo getPuppetInfo(ObjectId id) {
        return puppetRepository.findById(id, PuppetInfo.class);
    }

    public void updatePuppetInfo(ObjectId id, PuppetInfo info) {
        operateAndSavePuppet(id, puppet -> {
            if (info.nickname != null) puppet.nickname = info.nickname;
            if (info.introduction != null) puppet.introduction = info.introduction;
            if (info.contacts != null) puppet.contacts = info.contacts;
        });
    }

    public void updatePuppetAvatar(ObjectId id, String avatarUrl) {
        operateAndSavePuppet(id, puppet -> {
            puppet.avatarUrl = avatarUrl;
        });
    }
}
