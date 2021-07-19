package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OfficialNotificationRepository extends MongoRepository<OfficialNotification, ObjectId> {

    List<OfficialNotification> findByUserId(ObjectId userId);

    OfficialNotification findByUserIdAndId(ObjectId userId, ObjectId id);
}
