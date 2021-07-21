package com.leader.api.service.admin;

import com.leader.api.util.component.ClientDataUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminIdService {

    public static final String ADMIN_ID = "admin_id";

    private final ClientDataUtil clientDataUtil;

    @Autowired
    public AdminIdService(ClientDataUtil clientDataUtil) {
        this.clientDataUtil = clientDataUtil;
    }

    public void setCurrentAdminId(ObjectId adminId) {
        clientDataUtil.set(ADMIN_ID, adminId);
    }

    public ObjectId getCurrentAdminId() {
        return clientDataUtil.get(ADMIN_ID, ObjectId.class);
    }

    public void clearCurrentAdminId() {
        clientDataUtil.remove(ADMIN_ID);
    }

    public boolean currentAdminExists() {
        return getCurrentAdminId() != null;
    }
}
