package com.leader.api.service.org.report;

import com.leader.api.data.org.report.OrgReport;
import com.leader.api.data.org.report.OrgReportRepository;
import com.leader.api.service.org.OrganizationService;
import org.springframework.stereotype.Service;

@Service
public class OrgReportService {

    private final OrganizationService organizationService;
    private final OrgReportRepository reportRepository;

    public OrgReportService(OrganizationService organizationService, OrgReportRepository reportRepository) {
        this.organizationService = organizationService;
        this.reportRepository = reportRepository;
    }

    public void sendReport(OrgReport report) {
        organizationService.assertOrganizationExists(report.organizationId);

        // ensure id field is not set
        OrgReport newReport = new OrgReport();
        newReport.organizationId = report.organizationId;
        newReport.senderUserId = report.senderUserId;
        newReport.description = report.description;
        newReport.imageUrls = report.imageUrls;

        reportRepository.insert(newReport);
    }
}
