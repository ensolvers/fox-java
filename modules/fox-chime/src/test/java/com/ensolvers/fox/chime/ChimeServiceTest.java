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
package com.ensolvers.fox.chime;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.chime.model.Attendee;
import com.amazonaws.services.chime.model.ListChannelMessagesResult;
import com.amazonaws.services.chime.model.ListChannelsResult;
import com.amazonaws.services.chime.model.Meeting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

class ChimeServiceTest {
	private ChimeService chimeService;
	/**
	 * NOTE: test disabled since Localstack Chime integration is not available
 	 */

	@Test
	@Disabled("Integration not available")
	void testChime() {
		String accessKey = "";
		String secretKey = "";
		Regions region = Regions.US_EAST_1;
		String chatAccessKey = "";
		String chatSecretKey = "";
		String appArn = "";

		ChimeService service = new ChimeService(new BasicAWSCredentials(accessKey, secretKey), region, appArn,
				new BasicAWSCredentials(chatAccessKey, chatSecretKey));

		service.listChannels("userArn");
		Meeting meeting = service.createMeeting("token");
		service.getMeeting(meeting.getMeetingId());
		Attendee attendee = service.joinMeeting("userId", meeting.getMeetingId());
		service.listChannels("userArn");
		Assertions.assertSame(service.getMeeting(meeting.getMeetingId()), meeting);
	}

	@Test
	@Disabled("integration not available")
	void shouldCreateAppInstance() {
		String appInstanceArn = this.chimeService.createAppInstance("New Instance");

		Assertions.assertFalse(appInstanceArn.isEmpty());
	}

	@Test
	@Disabled("integration not available")
	void createMeeting() {
		Meeting meeting = this.chimeService.createMeeting("Client_Token");

		Assertions.assertFalse(meeting.getMeetingId().isEmpty());
	}

	@Test
	@Disabled("integration not available")
	void getMeeting() {
		Meeting meeting = this.chimeService.createMeeting("Client_Token");

		Assertions.assertEquals(meeting.getMeetingId(),
						this.chimeService.getMeeting(meeting.getMeetingId()).getMeetingId());
	}

	@Test
	@Disabled("integration not available")
	void shouldJoinMeeting() {
		Meeting meeting = this.chimeService.createMeeting("Client_Token");
		this.chimeService.joinMeeting("USER_ID", meeting.getMeetingId());

		Assertions.assertEquals(meeting.getMeetingId(),
						this.chimeService.getMeeting(meeting.getMeetingId()).getMeetingId());
	}

	@Test
	@Disabled("integration not available")
	void shouldCreateUser() {
		String userARNWithMetadata =
						this.chimeService.createUser("USER_ID", "Full Name", "METADATA");

		String userARNWithoutMetadata =
						this.chimeService.createUser("USER_ID", "Full Name");
	}

	@Test
	@Disabled("integration not available")
	void shouldUpdateUser() {
		String userARNWithoutMetadata =
						this.chimeService.createUser("USER_ID", "Full Name");

		String updateUserARN =
						this.chimeService.updateUser(userARNWithoutMetadata,"Full Name","NEW_METADATA");

		Assertions.assertFalse(updateUserARN.isEmpty());
	}

	@Test
	@Disabled("integration not available")
	void shouldCreateChannel() {
		String creatorARN =
						this.chimeService.createUser("USER_ID", "Full Name");

		String channel = this.chimeService.createChannel("New Channel",creatorARN);

		String channelWithMetadata = this.chimeService.createChannel("Channel with Metadata", creatorARN);

		Assertions.assertFalse(channel.isEmpty());
		Assertions.assertFalse(channelWithMetadata.isEmpty());
	}

	@Test
	@Disabled("integration not available")
	void shouldAddMemberToChannel() {
		String creatorARN =
						this.chimeService.createUser("USER_ID", "Full Name");

		String memberARN =
						this.chimeService.createUser("MEMBER_ID", "Full Name");

		String channelARN = this.chimeService.createChannel("New Channel",creatorARN);
		try {
			this.chimeService.addMembersToChannel(channelARN, creatorARN, Collections.singletonList(memberARN));
		} catch (Exception e) {
			Assertions.fail();
		}
		Assertions.assertTrue(true);
	}

	@Test
	@Disabled("integration not available")
	void shouldUpdateChannel() {
		String creatorARN =
						this.chimeService.createUser("USER_ID", "Full Name");

		String channelARN = this.chimeService.createChannel("New Channel",creatorARN);

		String updatedChannelARN =
						this.chimeService.updateChannel(channelARN, creatorARN, "Updated Channel", "updated Metadata");

		Assertions.assertFalse(updatedChannelARN.isEmpty());
	}

	@Test
	@Disabled("integration not available")
	void shouldListChannels() {
		String creatorARN =
						this.chimeService.createUser("USER_ID", "Full Name");

		String channelARN = this.chimeService.createChannel("New Channel",creatorARN);

		ListChannelsResult channelsResult = this.chimeService.listChannels(creatorARN);

		Assertions.assertFalse(channelsResult.getChannels().isEmpty());
		Assertions.assertEquals(200, channelsResult.getSdkHttpMetadata().getHttpStatusCode());
	}

	@Test
	@Disabled("integration not available")
	void shouldListMessages() {
		String creatorARN =
						this.chimeService.createUser("USER_ID", "Full Name");

		String channelARN = this.chimeService.createChannel("New Channel",creatorARN);

		Date before = new Date();
		Date after = new Date(before.getTime() - 100L);

		ListChannelMessagesResult msgList= this.chimeService.listMessages(
						creatorARN,
						channelARN,
						10,
						"0",
						before,
						after);
		// Will only work once a message is sent
		Assertions.assertFalse(msgList.getChannelMessages().isEmpty());
	}

	@Test
	@Disabled("integration not available")
	void shouldGetWebSocketConnection() {
		String webSocketConnection =
						this.chimeService.getWebSocketConnection("AppInstanceUserARN", new Date());

		Assertions.assertFalse(webSocketConnection.isEmpty());
	}
}
