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

    private void swapIds(OrgType type1, OrgType type2) {
        ObjectId temp;
        temp = type1.id;
        type1.id = type2.id;
        type2.id = temp;
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

    public void moveUpOrgType(ObjectId typeId) {
        synchronized (typeRepository) {
            List<OrgType> types = getOrgTypes();
            for (int i = 0; i < types.size(); i++) {
                OrgType type1 = types.get(i);
                if (type1.id.equals(typeId)) {
                    if (i > 0) {
                        OrgType type2 = types.get(i - 1);
                        swapIds(type1, type2);
                        typeRepository.save(type1);
                        typeRepository.save(type2);
                    }
                    return;
                }
            }
        }
    }

    public void moveDownOrgType(ObjectId typeId) {
        synchronized (typeRepository) {
            List<OrgType> types = getOrgTypes();
            for (int i = 0; i < types.size(); i++) {
                OrgType type1 = types.get(i);
                if (type1.id.equals(typeId)) {
                    if (i < types.size() - 1) {
                        OrgType type2 = types.get(i + 1);
                        swapIds(type1, type2);
                        typeRepository.save(type1);
                        typeRepository.save(type2);
                    }
                    return;
                }
            }
        }
    }
}
