package com.leader.api.service.admin.org;

import com.leader.api.data.org.report.OrgReportDetail;
import com.leader.api.data.org.report.OrgReportOverview;
import com.leader.api.data.org.report.OrgReportRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminOrgReportService {

    private final OrgReportRepository reportRepository;

    @Autowired
    public AdminOrgReportService(OrgReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<OrgReportOverview> getReports() {
        return reportRepository.lookupByQuery(new Document(), Pageable.unpaged(), OrgReportOverview.class);
    }

    public OrgReportDetail getReportDetail(ObjectId reportId) {
        return reportRepository.lookupByQuery(new Document("_id", reportId), Pageable.unpaged(), OrgReportDetail.class).get(0);
    }
}
