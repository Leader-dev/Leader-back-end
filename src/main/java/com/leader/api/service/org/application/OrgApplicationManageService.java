package com.leader.api.service.org.application;

import com.leader.api.data.org.application.OrgApplicationRepository;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrgApplicationManageService {

    private final OrgAuthorizationService authorizationService;
    private final OrgApplicationRepository applicationRepository;

    @Autowired
    public OrgApplicationManageService(OrgAuthorizationService authorizationService,
                                       OrgApplicationRepository applicationRepository) {
        this.authorizationService = authorizationService;
        this.applicationRepository = applicationRepository;
    }
}
