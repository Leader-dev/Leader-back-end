package com.leader.api.data.app;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppInfoRepository extends MongoRepository<AppInfo, ObjectId> {

    AppInfo findFirstBy();
}
