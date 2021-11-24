package com.ensolvers.fox.sns;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SNSServiceTest {

	@Disabled("Real credentials need to be provided")
	@Test
	void testSNS() {
		String accessKey = "";
		String secretKey = "";
		Regions region = Regions.US_EAST_1;

		AmazonSNS client = AmazonSNSClient.builder()
				.withCredentials(new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))).withRegion(region).build();

		SNSService service = new SNSService(client);

		String senderId = "Ensolvers";
		String phoneNumber = "";
		String message = "Test SMS message";
		double maxPrice = 0.01;

		String smsId = service.sendSMSMessage(senderId, phoneNumber, message, false, maxPrice);
		Assertions.assertFalse(smsId.isEmpty());
	}
}
