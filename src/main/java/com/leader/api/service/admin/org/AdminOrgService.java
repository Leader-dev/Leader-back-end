package com.leader.api.service.admin.org;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.member.OrgJoinedOverview;
import com.leader.api.data.org.type.OrgType;
import com.leader.api.data.org.type.OrgTypeRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminOrgService {

    private final OrganizationRepository organizationRepository;
    private final OrgTypeRepository typeRepository;

    @Autowired
    public AdminOrgService(OrganizationRepository organizationRepository, OrgTypeRepository typeRepository) {
        this.organizationRepository = organizationRepository;
        this.typeRepository = typeRepository;
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

    public List<OrgType> getOrgTypes() {
        return typeRepository.findAll();
    }

    public void saveOrgType(OrgType type) {
        if (type.id == null) {
            typeRepository.insert(type);
        } else {
            typeRepository.save(type);
        }
    }

    public void deleteOrgType(ObjectId typeId) {
        typeRepository.deleteById(typeId);
    }
}
