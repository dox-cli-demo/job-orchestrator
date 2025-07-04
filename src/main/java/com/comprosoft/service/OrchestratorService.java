// src/main/java/com/example/orchestrator/service/OrchestratorService.java
package com.comprosoft.service;

import java.io.StringWriter;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrchestratorService {

	private final Configuration freemarkerConfig;

	public OrchestratorService() throws Exception {
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
		freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

	}

	public void runJobTasks(Long requestId, int totaltasks) throws Exception {
		for (int i = 1; i <= totaltasks; i++) {
			createJob(requestId, i);
		}
	}

	public String renderToString(Long requestId, int taskId) throws Exception {
		Template template = freemarkerConfig.getTemplate("job-template.ftl");

		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put("requestId", requestId);
		dataModel.put("taskId", taskId);

		try (StringWriter writer = new StringWriter()) {
			template.process(dataModel, writer);
			return writer.toString();
		}
	}

	private void createJob(Long requestId, int taskId) throws Exception {
		String jobTemplateYaml = renderToString(requestId, taskId);

		// Deserialize rendered YAML to Fabric8 Job object
		Job job = Serialization.unmarshal(jobTemplateYaml, Job.class);

		// Create the job in Kubernetes
		try (KubernetesClient client = new KubernetesClientBuilder().build()) {
			String namespace = client.getNamespace();
			if (namespace == null) {
				namespace = "default"; // fallback
			}
			client.resource(job).inNamespace(namespace).create();
			log.info("Job created for taskId: {} in namespace : {} ", taskId, namespace);
		}
	}
}
