package com.leader.api.service.admin.org;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.member.OrgJoinedOverview;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminOrgService {

    private final OrganizationRepository organizationRepository;

    @Autowired
    public AdminOrgService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Page<OrgJoinedOverview> getOrganizations() {
        return organizationRepository.findByQuery(new Document(), Pageable.unpaged(), OrgJoinedOverview.class);
    }

    public void updateOrganization(Organization info) {
        organizationRepository.findById(info.id).ifPresent(organization -> {
            organization.instituteAuth = info.instituteAuth;
            organization.status = info.status;
            organizationRepository.save(organization);
        });
    }
}
