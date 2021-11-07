package com.ensolvers.fox.ses;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class SESServiceTest {

	@Container
	public LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
			.withServices(LocalStackContainer.Service.SES);

	@Test
	public void testSES() {
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
				.withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SES))
				.withCredentials(localstack.getDefaultCredentialsProvider()).build();

		SESService service = new SESService(client);

		String from = "";
		String[] to = { "" };
		String body = "Test body";
		String subject = "Test subject";

		service.sendEmail(from, subject, body, false, to);
	}
}
