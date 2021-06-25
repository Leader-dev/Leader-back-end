package com.leader.api.controller.org.manage;

import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.service.user.UserService;
import com.leader.api.service.util.AuthCodeService;
import com.leader.api.service.util.PasswordService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leader.api.service.org.authorization.OrgAuthority.PRESIDENT_TRANSFER;

@RestController
@RequestMapping("/org/manage/transfer")
public class OrgTransferController {

    private final OrgAuthorizationService authorizationService;
    private final OrgStructureService structureService;
    private final UserIdService userIdService;
    private final OrgMemberIdService memberIdService;
    private final AuthCodeService authCodeService;
    private final PasswordService passwordService;
    private final UserService userService;

    @Autowired
    public OrgTransferController(OrgAuthorizationService authorizationService, OrgStructureService structureService,
                                 UserIdService userIdService, OrgMemberIdService memberIdService,
                                 AuthCodeService authCodeService, PasswordService passwordService,
                                 UserService userService) {
        this.authorizationService = authorizationService;
        this.structureService = structureService;
        this.userIdService = userIdService;
        this.memberIdService = memberIdService;
        this.authCodeService = authCodeService;
        this.passwordService = passwordService;
        this.userService = userService;
    }

    public static class QueryObject {
        public ObjectId newPresidentUserId;
        public String authcode;
        public String password;
    }

    @PostMapping("/")
    public Document transferPresident(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(PRESIDENT_TRANSFER);
        authorizationService.assertCurrentMemberCanManageMember(queryObject.newPresidentUserId);

        ObjectId userId = userIdService.getCurrentUserId();
        String phone = userService.getUserInfo(userId).phone;

        if (queryObject.password != null) {
            String password = passwordService.decrypt(queryObject.password);
            if (!userService.validateUser(phone, password)) {
                return new ErrorResponse("password_incorrect");
            }
        } else if (queryObject.authcode != null) {
            if (!authCodeService.validateAuthCode(phone, queryObject.authcode)) {
                return new ErrorResponse("authcode_incorrect");
            }
            authCodeService.removeAuthCodeRecord(phone);
        } else {
            throw new InternalErrorException("Expect password or authcode attribute in request");
        }

        ObjectId memberId = memberIdService.getCurrentMemberId();
        structureService.setMemberToPresident(queryObject.newPresidentUserId);
        structureService.removeMemberFromPresident(memberId);

        return new SuccessResponse();
    }
}
