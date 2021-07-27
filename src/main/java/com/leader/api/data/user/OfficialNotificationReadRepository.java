package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OfficialNotificationReadRepository extends MongoRepository<OfficialNotificationRead, ObjectId> {

    boolean existsByUserIdAndNotificationId(ObjectId userId, ObjectId notificationId);

    void deleteByNotificationId(ObjectId notificationId);
}
