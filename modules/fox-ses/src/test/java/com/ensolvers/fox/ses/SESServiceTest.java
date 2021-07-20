package com.ensolvers.fox.ses;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SESServiceTest {

  @Test
  @Disabled
  public void testSES() {
    String accessKey = "";
    String secretKey = "";
    Regions region = Regions.US_EAST_1;

    AmazonSimpleEmailService client =
        AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(
                new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .withRegion(region)
            .build();

    SESService service = new SESService(client);

    String from = "";
    String[] to = {""};
    String body = "Test body";
    String subject = "Test subject";

    service.sendEmail(from, subject, body, false, to);
  }
}
