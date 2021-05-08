package com.leader.api.data.org;

import com.leader.api.util.Regex;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface OrganizationRepository extends MongoRepository<Organization, ObjectId> {

    Organization findByNumberId(String numberId);

    Organization findByIdAndStatus(ObjectId id, String status);

    @Query("?0")
    <T> Page<T> findByQuery(
            Document query,
            Pageable pageable,
            Class<T> type
    );

    default Page<OrganizationLobbyOverview> findRunningOrganizationsByQueryObject(OrganizationQueryObject queryObject) {
        Document query = new Document("status", "running");
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
        return findByQuery(
                query,
                PageRequest.of(queryObject.pageNumber, queryObject.pageSize),
                OrganizationLobbyOverview.class
        );
    }

    default Organization insertNewOrganization(Organization newOrganization) {
        Organization org = new Organization();

        // copy items, only these can be set by the user
        org.name = newOrganization.name;
        org.address = newOrganization.address;
        org.introduction = newOrganization.introduction;
        org.phone = newOrganization.phone;
        org.email = newOrganization.email;
        org.typeAliases = newOrganization.typeAliases;
        org.posterUrl = newOrganization.posterUrl;

        // set items
        // TODO Generate number ID
        org.status = "pending";  // must be pending state
        org.memberCount = 1L;  // the user is the first member

        return insert(newOrganization);
    }
}
