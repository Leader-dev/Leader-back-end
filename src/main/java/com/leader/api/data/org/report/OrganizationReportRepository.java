package com.leader.api.data.org.report;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationReportRepository extends MongoRepository<OrganizationReport, ObjectId> {

    default OrganizationReport insertNewReport(OrganizationReport report) {
        OrganizationReport newReport = new OrganizationReport();
        newReport.organizationId = report.organizationId;
        newReport.senderUserId = report.senderUserId;
        newReport.description = report.description;
        newReport.imageUrls = report.imageUrls;
        return insert(report);
    }
}
