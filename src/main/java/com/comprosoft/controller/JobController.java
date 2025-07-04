package com.comprosoft.controller;

import java.util.Random;

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
			int requestId = new Random().nextInt(Integer.MAX_VALUE); // Range: 0 to 2,147,483,647
			orchestratorService.runJobTasks(requestId + "", tasks);
			return "Started " + tasks + " job(s).  ID : "+ requestId;
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
}