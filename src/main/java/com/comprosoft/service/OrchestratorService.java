// src/main/java/com/example/orchestrator/service/OrchestratorService.java
package com.comprosoft.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;

@Service
public class OrchestratorService {

	private static final String TEMPLATE_PATH = "src/main/resources/templates/job-template.ftl";

	private final Configuration freemarkerConfig;

	public OrchestratorService() throws Exception {
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	public void runJobTasks(Long requestId, int totaltasks) throws Exception {
		for (int i = 1; i <= totaltasks; i++) {
			createJob(requestId, i);
		}
	}

	private void createJob(Long requestId, int taskId) throws Exception {
		// Load FreeMarker template string from file
		String templateContent = Files.readString(Paths.get(TEMPLATE_PATH));

		// Set template variables
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put("requestId", requestId);
		dataModel.put("taskId", taskId);

		// Create and process FreeMarker template
		Template template = new Template("jobTemplate", new StringReader(templateContent), freemarkerConfig);
		StringWriter renderedYaml = new StringWriter();
		template.process(dataModel, renderedYaml);

		// Deserialize rendered YAML to Fabric8 Job object
		Job job = Serialization.unmarshal(renderedYaml.toString(), Job.class);

		// Create the job in Kubernetes
		try (KubernetesClient client = new KubernetesClientBuilder().build()) {
			String namespace = client.getNamespace();
			if (namespace == null) {
				namespace = "default"; // fallback
			}
			client.resource(job).inNamespace(namespace).create();
			System.out.println("Job created for taskId: " + taskId);
		}
	}
}
