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
package com.ensolvers.fox.email;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * A Test case for {@link EmailService}
 *
 * @author Esteban Robles Luna
 */
class EmailServiceTest {
	private final String awsSES_HOST = "host";
	private final Integer awsSES_PORT = 465;
	private final String smtpUsername = "username";
	private final String smtpPassword = "password";
	private final String senderMail = "info@ensolvers.com";

	private final String subject = "Hola";
	private final String body = "Hola esteban como estas?";
	private final String recipientMail = "esteban.roblesluna@gmail.com";

	@Test
	@Disabled("disabled")
	void testEmail() throws Exception {
		EmailService service = new EmailService(awsSES_HOST, awsSES_PORT, smtpUsername, smtpPassword, senderMail);
		service.sendMailTo(recipientMail, subject, body);

		Assertions.assertFalse(service.toString().isEmpty());
	}
}
