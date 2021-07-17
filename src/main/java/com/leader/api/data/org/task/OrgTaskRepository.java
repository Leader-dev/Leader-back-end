package com.leader.api.data.org.task;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface OrgTaskRepository extends MongoRepository<OrgTask, ObjectId> {


    @Aggregation(pipeline= {
            "{" +
                    "$match: {publishUserId: ?0}" +
                    "}",
            "{" +
                    "$lookup: { " +
                    "from: 'org_member'," +
                    "localField: 'publishUserId'," +
                    "foreignField: '_id'," +
                    "as: 'senderMemberInfo'" +
                    "}" +
            "}",
            "{" +
                    "$unwind: '$senderMemberInfo'" +
                    "}",

    })
    List<OrgTaskOverview> lookupByPublishUserId (ObjectId publishUserId);


}
