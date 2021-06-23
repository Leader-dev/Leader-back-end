package com.leader.api.service.org;

import com.leader.api.data.org.OrgPublicInfo;
import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    private void copyValidItemsTo(Organization target, OrgPublicInfo source) {
        // copy items, only these can be set by the user
        target.name = source.name;
        target.address = source.address;
        target.introduction = source.introduction;
        target.phone = source.phone;
        target.email = source.email;
        target.typeAliases = source.typeAliases;
        target.posterUrl = source.posterUrl;
    }

    private Organization copyValidItems(OrgPublicInfo organization) {
        Organization org = new Organization();
        copyValidItemsTo(org, organization);
        return org;
    }

    public boolean organizationExists(ObjectId organizationId) {
        return organizationRepository.existsById(organizationId);
    }

    public void assertOrganizationExists(ObjectId organizationId) {
        if (!organizationExists(organizationId)) {
            throw new InternalErrorException("Organization not exist");
        }
    }

    public Organization createNewOrganization(OrgPublicInfo newOrganization) {
        Organization org = copyValidItems(newOrganization);

        // set items
        // TODO Generate number ID
        org.status = "pending";  // must be pending state
        org.memberCount = 0L;  // no member is initially in the organization

        return organizationRepository.insert(org);
    }

    public void updateOrganizationPublicInfo(ObjectId organizationId, OrgPublicInfo publicInfo) {
        organizationRepository.findById(organizationId).ifPresent(org -> {
            copyValidItemsTo(org, publicInfo);
            organizationRepository.save(org);
        });
    }

    public Organization getOrganization(ObjectId organizationId) {
        return organizationRepository.findFirstById(organizationId);
    }

    public OrgPublicInfo getPublicInfo(ObjectId organizationId) {
        return organizationRepository.findFirstById(organizationId);
    }
}
