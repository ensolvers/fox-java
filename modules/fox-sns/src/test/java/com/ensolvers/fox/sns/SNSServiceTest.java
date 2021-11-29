package com.ensolvers.fox.sns;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SNSServiceTest {

	private String SENDER_ID = "Ensolvers";
	private String RECIPIENT_NUMBER = "";
	private String MESSAGE = "Test SMS message";

	private static String AWS_ACCESS_KEY_ID;
	private static String AWS_ACCESS_KEY_SECRET;
	private static String AWS_REGION;

	@BeforeAll
	public static void initialize() throws ConfigurationException {
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.load("snsService.properties");

		AWS_ACCESS_KEY_ID = configuration.getString("aws.access.key.id");
		AWS_ACCESS_KEY_SECRET = configuration.getString("aws.access.key.secret");
		AWS_REGION = configuration.getString("aws.region");
	}

	@Disabled("403 Security token")
	@Test
	void testSNS() {

		AmazonSNS client = AmazonSNSClient.builder()
						.withCredentials(new StaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_ACCESS_KEY_SECRET)))
						.withRegion(AWS_REGION)
						.build();

		SNSService service = new SNSService(client);

		double maxPrice = 0.01;

		String smsId = service.sendSMSMessage(SENDER_ID, RECIPIENT_NUMBER, MESSAGE, false, maxPrice);
		Assertions.assertFalse(smsId.isEmpty());
	}
}
