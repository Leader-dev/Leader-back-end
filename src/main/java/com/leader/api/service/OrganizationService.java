package com.leader.api.service;

import com.leader.api.data.org.*;
import com.leader.api.data.org.membership.OrganizationJoinedOverview;
import com.leader.api.data.org.membership.OrganizationMembership;
import com.leader.api.data.org.membership.OrganizationMembershipRepository;
import com.leader.api.data.org.report.OrganizationReport;
import com.leader.api.data.org.report.OrganizationReportRepository;
import com.leader.api.data.org.type.OrganizationTypeProject;
import com.leader.api.data.org.type.OrganizationTypeRepository;
import com.leader.api.util.Regex;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    @Autowired
    private OrganizationTypeRepository typeRepository;

    @Autowired
    private OrganizationReportRepository reportRepository;

    private Organization insertNewOrganization(Organization newOrganization) {
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
        org.memberCount = 0L;  // no member is initially in the organization

        return organizationRepository.insert(org);
    }

    private void insertNewMembership(ObjectId organizationId, ObjectId userid) {
        OrganizationMembership existing = membershipRepository.findByOrganizationIdAndUserId(organizationId, userid);
        if (existing != null) {
            return;
        }
        OrganizationMembership orgMembership = new OrganizationMembership();
        orgMembership.organizationId = organizationId;
        orgMembership.userId = userid;
        membershipRepository.insert(orgMembership);
    }

    private void updateOrganizationMemberCount(ObjectId organizationId) {
        organizationRepository.findById(organizationId).ifPresent(organization -> {
            organization.memberCount = membershipRepository.countByOrganizationId(organizationId);
            organizationRepository.save(organization);
        });
    }

    public boolean organizationExists(ObjectId organizationId) {
        return organizationRepository.existsById(organizationId);
    }

    public void assertOrganizationExists(ObjectId organizationId) {
        if (!organizationExists(organizationId)) {
            throw new RuntimeException("Organization not exist");
        }
    }

    public Document getTypeAliasMapping() {
        // find all types that contains alias field
        List<OrganizationTypeProject> types = typeRepository.findAllByAliasNotNull(OrganizationTypeProject.class);

        // convert types from object list to key-value-pair object, with alias being the key
        Document typesMapping = new Document();
        types.forEach(t -> typesMapping.append(t.alias, t));
        return typesMapping;
    }

    public void createOrganization(Organization newOrganization, ObjectId userid) {
        Organization insertedOrganization = insertNewOrganization(newOrganization);
        joinOrganization(insertedOrganization.id, userid);
    }

    public void joinOrganization(ObjectId organizationId, ObjectId userid) {
        assertOrganizationExists(organizationId);
        insertNewMembership(organizationId, userid);
        updateOrganizationMemberCount(organizationId);
    }

    public <T> List<T> findOrganizationsByNumber(int count, Class<T> type) {
        Page<T> list = organizationRepository.findByQuery(
                new Document(),
                PageRequest.of(0, count),
                type
        );
        return list.getContent();
    }

    public Organization getOrganization(ObjectId organizationId) {
        assertOrganizationExists(organizationId);
        return organizationRepository.findById(organizationId).orElse(null);
    }

    public Page<OrganizationLobbyOverview> findRunningOrganizationsByQueryObject(OrganizationQueryObject queryObject) {
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
        return organizationRepository.findByQuery(
                query,
                PageRequest.of(queryObject.pageNumber, queryObject.pageSize),
                OrganizationLobbyOverview.class
        );
    }

    public List<OrganizationJoinedOverview> findJoinedOrganizations(ObjectId userid) {
        return membershipRepository.lookupJoinedOrganizationsByUserId(userid);
    }

    public void sendReport(OrganizationReport report) {
        assertOrganizationExists(report.organizationId);
        OrganizationReport newReport = new OrganizationReport();
        newReport.organizationId = report.organizationId;
        newReport.senderUserId = report.senderUserId;
        newReport.description = report.description;
        newReport.imageUrls = report.imageUrls;
        reportRepository.insert(report);
    }
}
