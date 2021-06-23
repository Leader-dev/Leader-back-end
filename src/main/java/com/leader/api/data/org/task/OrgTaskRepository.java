package com.leader.api.data.org.task;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrgTaskRepository extends MongoRepository<OrgTask, ObjectId> {
}
