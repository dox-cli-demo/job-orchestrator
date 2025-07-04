package com.comprosoft.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comprosoft.service.OrchestratorService;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final OrchestratorService orchestratorService;

    public JobController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/run")
    public String runTasks(@RequestParam int tasks) {
        try {
        	long requestId = Math.abs(UUID.randomUUID().getMostSignificantBits());

            orchestratorService.runJobTasks(requestId, tasks);
            return "Started " + tasks + " job(s).";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}