package com.leader.api.service.admin.user;

import com.leader.api.data.user.UserAdminOverview;
import com.leader.api.data.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    @Autowired
    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserAdminOverview> getAllUsers() {
        return userRepository.findAllBy(Pageable.unpaged(), UserAdminOverview.class);
    }
}
