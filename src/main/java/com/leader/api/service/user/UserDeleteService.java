package com.leader.api.service.user;

import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.trend.puppet.PuppetRepository;
import com.leader.api.data.user.User;
import com.leader.api.data.user.UserRepository;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.service.service.ImageService;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class UserDeleteService {

    private final long USER_DELETE_PERIOD_DAYS = 7;
    private final long USER_DELETE_PERIOD = USER_DELETE_PERIOD_DAYS * 24 * 60 * 60 * 1000;

    private final UserRepository userRepository;
    private final PuppetRepository puppetRepository;
    private final OrgMemberRepository memberRepository;
    private final OrgStructureService structureService;
    private final ImageService imageService;
    private final DateUtil dateUtil;

    public UserDeleteService(UserRepository userRepository, PuppetRepository puppetRepository, OrgMemberRepository memberRepository,
                             OrgStructureService structureService, ImageService imageService, DateUtil dateUtil) {
        this.userRepository = userRepository;
        this.puppetRepository = puppetRepository;
        this.memberRepository = memberRepository;
        this.structureService = structureService;
        this.imageService = imageService;
        this.dateUtil = dateUtil;
    }

    public void setDeleteUser(ObjectId userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.deleteStartDate = dateUtil.getTodayZero();
            userRepository.save(user);
        });
    }

    public void withdrawDeleteUser(ObjectId userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.deleteStartDate = null;
            userRepository.save(user);
        });
    }

    public Date getUserDeleteDate(ObjectId userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.deleteStartDate == null) {
            return null;
        }
        return new Date(user.deleteStartDate.getTime() + USER_DELETE_PERIOD);
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteUsers() {
        Date deleteStartBefore = dateUtil.getDateBefore(USER_DELETE_PERIOD);
        List<User> users = userRepository.findByDeleteStartDateBefore(deleteStartBefore);
        System.out.println("Start cleaning up users:");
        users.forEach(user -> {
            System.out.println(user.id);
            deleteUser(user.id);
        });
        System.out.println("Cleaning up ended.");
    }

    private void deleteUser(ObjectId userId) {
        String avatarUrl;

        // remove puppet info
        avatarUrl = puppetRepository.findByUserId(userId).avatarUrl;
        puppetRepository.deleteByUserId(userId);
        imageService.deleteImage(avatarUrl);

        // quit all organizations
        List<OrgMember> members = memberRepository.findAllByUserId(userId);
        members.forEach(member -> structureService.dismissMember(member.id));

        // remove user from database
        avatarUrl = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User should exist in this step")).avatarUrl;
        userRepository.deleteById(userId);
        imageService.deleteImage(avatarUrl);
    }
}
