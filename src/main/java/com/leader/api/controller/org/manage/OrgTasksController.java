package com.leader.api.controller.org.manage;

import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/org/manage/tasks")
public class OrgTasksController {

    @PostMapping("/list")
    public Document listTasks() {
        return new SuccessResponse();
    }

    @PostMapping("/detail")
    public Document taskDetail() {
        return new SuccessResponse();
    }

    @PostMapping("/submit")
    public Document submitTaskResult() {
        return new SuccessResponse();
    }

    @PostMapping("/publish")
    public Document publishTask() {
        return new SuccessResponse();
    }

    @PostMapping("/list-published")
    public Document listPublishedTasks() {
        return new SuccessResponse();
    }

    @PostMapping("/not-submitted-members")
    public Document listSubmitted() {
        return new SuccessResponse();
    }

    @PostMapping("/submitted-members")
    public Document listNotSubmitted() {
        return new SuccessResponse();
    }

    @PostMapping("/reply-submission")
    public Document replyToSubmission() {
        return new SuccessResponse();
    }
}
