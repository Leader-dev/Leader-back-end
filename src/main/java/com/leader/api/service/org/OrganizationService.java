package com.leader.api.service.org;

import com.leader.api.data.org.*;
import com.leader.api.service.util.SecureService;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.leader.api.data.org.Organization.PENDING;

@Service
public class OrganizationService {

    public static final int ORG_NUMBER_ID_LENGTH = 6;

    private final OrganizationRepository organizationRepository;
    private final SecureService secureService;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, SecureService secureService) {
        this.organizationRepository = organizationRepository;
        this.secureService = secureService;
    }

    private void copyValidItemsTo(Organization target, OrgPublicInfo source) {
        // copy items, only these can be set by the user
        target.name = source.name;
        target.address = source.address;
        target.instituteName = source.instituteName;
        target.introduction = source.introduction;
        target.phone = source.phone;
        target.email = source.email;
        target.typeAliases = source.typeAliases;
    }

    private Organization copyValidItems(OrgPublicInfo organization) {
        Organization newOrganization = new Organization();
        copyValidItemsTo(newOrganization, organization);
        return newOrganization;
    }

    public boolean organizationExists(ObjectId orgId) {
        return organizationRepository.existsById(orgId);
    }

    public void assertOrganizationExists(ObjectId orgId) {
        if (!organizationExists(orgId)) {
            throw new InternalErrorException("Organization not exist");
        }
    }

    public Organization createNewOrganization(OrgPublicInfo orgInfo) {
        Organization newOrganization = copyValidItems(orgInfo);
        newOrganization.posterUrl = orgInfo.posterUrl;

        // set items
        newOrganization.status = PENDING;  // must be pending state
        newOrganization.memberCount = 0L;  // no member is initially in the organization
        newOrganization.applicationScheme = new OrgApplicationScheme();  // new application scheme object
        newOrganization.receivedApplicationCount = 0;  // no application is initially sent to organization

        synchronized (organizationRepository) {
            newOrganization.numberId = secureService.generateRandomNumberId(
                    ORG_NUMBER_ID_LENGTH,
                    organizationRepository::existsByNumberId
            );
            return organizationRepository.insert(newOrganization);
        }
    }

    public void updateOrganizationPublicInfo(ObjectId orgId, OrgPublicInfo publicInfo) {
        organizationRepository.findById(orgId).ifPresent(organization -> {
            copyValidItemsTo(organization, publicInfo);
            organizationRepository.save(organization);
        });
    }

    public void updateOrganizationPoster(ObjectId orgId, String posterUrl) {
        organizationRepository.findById(orgId).ifPresent(organization -> {
            organization.posterUrl = posterUrl;
            organizationRepository.save(organization);
        });
    }

    public OrgDetail getOrganizationDetail(ObjectId orgId) {
        return organizationRepository.findById(orgId, OrgDetail.class);
    }

    public OrgPublicInfo getPublicInfo(ObjectId orgId) {
        return organizationRepository.findById(orgId, OrgPublicInfo.class);
    }
}
