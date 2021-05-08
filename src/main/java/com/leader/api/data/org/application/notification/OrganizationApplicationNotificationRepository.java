package com.leader.api.data.org.application.notification;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationApplicationNotificationRepository
        extends MongoRepository<OrganizationApplicationNotification, ObjectId> {
}
