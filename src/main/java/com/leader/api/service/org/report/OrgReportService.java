package com.leader.api.service.org.report;

import com.leader.api.data.org.report.OrgReport;
import com.leader.api.data.org.report.OrgReportRepository;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.util.component.DateUtil;
import org.springframework.stereotype.Service;

@Service
public class OrgReportService {

    private final OrganizationService organizationService;
    private final OrgReportRepository reportRepository;
    private final DateUtil dateUtil;

    public OrgReportService(OrganizationService organizationService, OrgReportRepository reportRepository, DateUtil dateUtil) {
        this.organizationService = organizationService;
        this.reportRepository = reportRepository;
        this.dateUtil = dateUtil;
    }

    public void sendReport(OrgReport report) {
        organizationService.assertOrganizationExists(report.orgId);

        // ensure id field is not set
        OrgReport newReport = new OrgReport();
        newReport.orgId = report.orgId;
        newReport.senderUserId = report.senderUserId;
        newReport.sendDate = dateUtil.getCurrentDate();
        newReport.description = report.description;
        newReport.imageUrls = report.imageUrls;

        reportRepository.insert(newReport);
    }
}
