package com.leader.api.data.org.task;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgTaskSubmissionRepository extends MongoRepository<OrgTaskSubmission, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
            "   "
    })
    List<OrgTaskSubmissionLookup> lookupSubmissionsByUserId(ObjectId userId);
}
