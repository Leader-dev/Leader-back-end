package com.leader.api.service.org.query;

import com.leader.api.data.org.OrgLobbyOverview;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.util.Regex;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgQueryService {

    private final OrganizationRepository organizationRepository;

    public OrgQueryService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public <T> List<T> findOrganizationsByNumber(int count, Class<T> type) {
        Page<T> list = organizationRepository.findByQuery(
                new Document(),
                PageRequest.of(0, count),
                type
        );
        return list.getContent();
    }

    public Page<OrgLobbyOverview> findRunningOrganizationsByQueryObject(OrgQueryObject queryObject) {
        Document query = new Document();
        query.append("status", "running");
        if (queryObject.numberId != null) {
            query.append("numberId", queryObject.queryName);
        }
        if (queryObject.queryName != null) {
            query.append("name", new Regex(".*" + queryObject.queryName + ".*"));
        }
        if (queryObject.typeAlias != null) {
            Document inCondition = new Document("$in", new String[]{queryObject.typeAlias});  // condition after $in must be a list
            query.append("typeAliases", inCondition);
        }
        Document memberCountCondition = new Document();
        if (queryObject.minMemberCount != null) {
            memberCountCondition.append("$gte", queryObject.minMemberCount);
        }
        if (queryObject.maxMemberCount != null) {
            memberCountCondition.append("$lte", queryObject.maxMemberCount);
        }
        if (memberCountCondition.size() > 0) {
            query.append("memberCount", memberCountCondition);
        }
        return organizationRepository.findByQuery(
                query,
                PageRequest.of(queryObject.pageNumber, queryObject.pageSize),
                OrgLobbyOverview.class
        );
    }
}
