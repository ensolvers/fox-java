/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.ses;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SES service takes care of sending emails using AWS SES
 *
 * @author Facundo Garbino
 */
public class SESService {

  private static final Logger logger = LoggerFactory.getLogger(SESService.class);
  private static final String LOG_PREFIX = "[AWS-SES]";

  private final AmazonSimpleEmailService client;

  public SESService(AmazonSimpleEmailService client) {
    this.client = client;
  }

  /**
   * Sends an email with the specified parameters
   *
   * @param fromEmail the email to sent it from (must be from a validated domain)
   * @param subject the email subject
   * @param bodyText the email body (might be plain text or HTML)
   * @param isHTML whether the email is HTML or plain text
   * @param toEmails an array of email addresses to send the email to
   * @return the message id of the result
   */
  public String sendEmail(
      String fromEmail, String subject, String bodyText, boolean isHTML, String... toEmails) {
    Body body = new Body();
    if (isHTML) {
      body.withHtml(new Content().withCharset("UTF-8").withData(bodyText));
    } else {
      body.withText(new Content().withCharset("UTF-8").withData(bodyText));
    }

    SendEmailRequest request =
        new SendEmailRequest()
            .withDestination(new Destination().withToAddresses(toEmails))
            .withMessage(
                new Message()
                    .withBody(body)
                    .withSubject(new Content().withCharset("UTF-8").withData(subject)))
            .withSource(fromEmail);

    SendEmailResult sendEmailResult = client.sendEmail(request);
    logger.info(
        String.format(
            "%s Email sent to %s, result: %s",
            LOG_PREFIX, Arrays.toString(toEmails), sendEmailResult));
    return sendEmailResult.getMessageId();
  }
}
