package com.leader.api.service.org.favorite;

import com.leader.api.data.org.OrgLobbyOverview;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.favorite.OrgFavoriteRecord;
import com.leader.api.data.org.favorite.OrgFavoriteRecordRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgFavoriteService {

    private final OrganizationRepository organizationRepository;
    private final OrgFavoriteRecordRepository favoriteRecordRepository;

    @Autowired
    public OrgFavoriteService(OrganizationRepository organizationRepository,
                              OrgFavoriteRecordRepository favoriteRecordRepository) {
        this.organizationRepository = organizationRepository;
        this.favoriteRecordRepository = favoriteRecordRepository;
    }

    public List<OrgLobbyOverview> listFavoriteOrganizations(ObjectId userId) {
        return favoriteRecordRepository.lookupOrganizationsByUserId(userId, OrgLobbyOverview.class);
    }

    public void addOrganizationToFavorite(ObjectId orgId, ObjectId userId) {
        if (organizationRepository.existsById(orgId) && !favoriteRecordRepository.existsByOrgIdAndUserId(orgId, userId)) {
            OrgFavoriteRecord record = new OrgFavoriteRecord();
            record.orgId = orgId;
            record.userId = userId;
            favoriteRecordRepository.save(record);
        }
    }

    public void removeOrganizationFromFavorite(ObjectId orgId, ObjectId userId) {
        favoriteRecordRepository.deleteByOrgIdAndUserId(orgId, userId);
    }

    public boolean isFavoriteOrganization(ObjectId orgId, ObjectId userId) {
        return favoriteRecordRepository.existsByOrgIdAndUserId(orgId, userId);
    }
}
