package com.leader.api.data.org.task;

import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;

@Document(collection = "org_task_submission")
public class OrgTaskSubmission {
    // All task information are stored in another class

    //                                               Returns: TaskDetail | SubmissionDetail
    public static String NOT_SUBMITTED = "not-submitted";//    Detail    |  null
    public static String PENDING = "pending";            //    Overview  |  Detail
    public static String PASSED = "passed";              //    Overview  |  Detail
    public static String REJECTED = "rejected";          //    Detail    |  Detail
    public static String CANCELLED = "cancelled";        //    Overview  |  Detail
                                                         // Tasks with task details allow to be submitted
    public static String[] allStatuses = {NOT_SUBMITTED, REJECTED, PENDING, PASSED, CANCELLED};
    public static String[] activeStatuses = {NOT_SUBMITTED, REJECTED};
    public static String[] inactiveStatuses = {PENDING, PASSED, CANCELLED};

    public static String[] getStatusCollection (String status) {
        if (Arrays.asList(allStatuses).contains(status)){
            return new String[]{status};
        }
        switch (status){
            case "all-status": return allStatuses;
            case "active": return activeStatuses;
            case "inactive": return inactiveStatuses;
            default: throw new InternalErrorException("Invalid Status Collection!");
        }
    }

    @Id
    public ObjectId id;
    public ObjectId taskId;
    public ObjectId memberId; // Member Submission

    public String status;

    public ArrayList<OrgTaskSubmissionAttempt> submissionAttempts;
}
