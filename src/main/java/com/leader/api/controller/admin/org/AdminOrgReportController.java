package com.leader.api.controller.admin.org;

import com.leader.api.service.admin.org.AdminOrgReportService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/org/report")
public class AdminOrgReportController {

    private final AdminOrgReportService reportService;

    @Autowired
    public AdminOrgReportController(AdminOrgReportService reportService) {
        this.reportService = reportService;
    }

    public static class QueryObject {
        public ObjectId reportId;
    }

    @PostMapping("/list")
    public Document listReports() {
        Document response = new SuccessResponse();
        response.append("reports", reportService.getReports());
        return response;
    }

    @PostMapping("/detail")
    public Document getReportDetail(@RequestBody QueryObject queryObject) {
        Document response = new SuccessResponse();
        response.append("detail", reportService.getReportDetail(queryObject.reportId));
        return response;
    }
}
