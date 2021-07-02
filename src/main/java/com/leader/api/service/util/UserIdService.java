package com.leader.api.service.util;

import com.leader.api.util.UserAuthException;
import com.leader.api.util.component.ClientDataUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserIdService {

    public static final String USER_ID = "user_id";

    private final ClientDataUtil clientDataUtil;

    @Autowired
    public UserIdService(ClientDataUtil clientDataUtil) {
        this.clientDataUtil = clientDataUtil;
    }

    public void setCurrentUserId(ObjectId userid) {
        clientDataUtil.set(USER_ID, userid);
    }

    public ObjectId getCurrentUserId() {
        ObjectId userId = clientDataUtil.get(USER_ID, ObjectId.class);
        if (userId == null) {
            throw new UserAuthException();
        }
        return userId;
    }

    public void clearCurrentUserId() {
        clientDataUtil.remove(USER_ID);
    }

    public boolean currentUserExists() {
        return getCurrentUserId() != null;
    }
}
