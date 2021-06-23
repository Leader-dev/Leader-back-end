package com.leader.api.controller.org.manage;

import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/org/manage/member/info")
public class OrgMemberInfoController {

    private final OrgMemberRepository membershipRepository;

    @Autowired
    public OrgMemberInfoController(OrgMemberRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public static class QueryObject {
        public ObjectId organizationId;
        public ObjectId userId;
    }

    @PostMapping("/")
    public Document showMemberInfo(@RequestBody QueryObject queryObject) {
        return new SuccessResponse();
    }
}
