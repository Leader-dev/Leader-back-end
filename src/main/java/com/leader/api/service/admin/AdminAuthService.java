package com.leader.api.service.admin;

import com.leader.api.data.admin.Admin;
import com.leader.api.data.admin.AdminInfo;
import com.leader.api.data.admin.AdminRepository;
import com.leader.api.service.util.SecureService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final SecureService secureService;

    @Autowired
    public AdminAuthService(AdminRepository adminRepository, SecureService secureService) {
        this.adminRepository = adminRepository;
        this.secureService = secureService;
    }

    public boolean usernameExists(String username) {
        return adminRepository.existsByUsername(username);
    }

    public boolean validateAdmin(String username, String password) {
        Admin admin = adminRepository.findByUsername(username);
        if (admin.password == null) {
            return true;
        }
        return secureService.matchesPassword(password, admin.password);
    }

    public ObjectId getAdminId(String username) {
        Admin admin = adminRepository.findByUsername(username);
        return admin.id;
    }

    public AdminInfo getAdminInfo(ObjectId adminId) {
        return adminRepository.findById(adminId, AdminInfo.class);
    }

    public void changePassword(ObjectId adminId, String password) {
        adminRepository.findById(adminId).ifPresent(admin -> {
            admin.password = secureService.encodePassword(password);
            adminRepository.save(admin);
        });
    }
}
