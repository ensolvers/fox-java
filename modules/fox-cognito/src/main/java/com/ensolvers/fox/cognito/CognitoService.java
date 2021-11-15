package com.ensolvers.fox.cognito;

import java.util.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

public class CognitoService {
	private final String userPoolId;
	private final String clientId;
	private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

	public CognitoService(String userPoolId, String clientId, String accessKey, String secretKey) {
		this.userPoolId = userPoolId;
		this.clientId = clientId;

		var awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

		this.cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).region(Region.US_EAST_1).build();
	}

	/**
	 *
	 * @param groupName	Group's name
	 * @param description	(Optional) Group description
	 * @param role Role of the Users included in the group
	 * @param precedence If user belongs to several Groups, determines which one has permission's priority
	 */
	public CreateGroupResponse createGroup(String groupName, String description, String role, Integer precedence) {
		CreateGroupRequest createGroupRequest =
						CreateGroupRequest.builder()
										.userPoolId(this.userPoolId)
										.groupName(groupName)
										.description(description)
										.roleArn(role)
										.precedence(precedence).build();

		return this.cognitoIdentityProviderClient.createGroup(createGroupRequest);
	}
	/**
	 * Add a user to a cognito group, the group and the user must exist in cognito
	 * or else it will fail
	 *
	 * @param username The user's username or email
	 * @param group    The previously defined group in cognito
	 * 
	 * @return An object with all the response data
	 */
	public AdminAddUserToGroupResponse addUserToGroup(String username, String group) {
		var adminAddUserToGroupRequest =
						AdminAddUserToGroupRequest.builder().userPoolId(this.userPoolId).username(username).groupName(group).build();

		return this.cognitoIdentityProviderClient.adminAddUserToGroup(adminAddUserToGroupRequest);
	}

	/**
	 * Removes the user from a specific user pool's group.
	 *
	 * @param username User to be removed
	 * @param groupName Group the user will be removed from
	 * @return {@link AdminRemoveUserFromGroupResponse}
	 */
	public AdminRemoveUserFromGroupResponse removeUserFromGroup(String username, String groupName) {
		AdminRemoveUserFromGroupRequest adminRemoveUserFromGroupRequest =
						AdminRemoveUserFromGroupRequest.builder()
										.userPoolId(this.userPoolId)
										.groupName(groupName)
										.username(username)
										.build();

		return this.cognitoIdentityProviderClient.adminRemoveUserFromGroup(adminRemoveUserFromGroupRequest);
	}
	/**
	 * Create a user with some password, this method is used for pools that use
	 * username/password authentication
	 *
	 * @param username         The user's username or email
	 * @param password         The password, the conditions for a password are
	 *                         defined in the user pool
	 * @param sendConfirmation True if you want the user to receive a confirmation
	 *                         mail
	 * 
	 * @return An object with all the response data
	 */
	public AdminCreateUserResponse createUserWithPassword(String username, String password, boolean sendConfirmation) {
		var request = AdminCreateUserRequest.builder().username(username).userPoolId(this.userPoolId).temporaryPassword(password);

		if (!sendConfirmation) {
			request.messageAction(MessageActionType.SUPPRESS);
		}

		return cognitoIdentityProviderClient.adminCreateUser(request.build());
	}

	/**
	 * Sign in, this method is used for pools that use username/password
	 * authentication
	 *
	 * @param username The user's username or email
	 * @param password The user's password
	 * 
	 * @return An object with all the response data, this object contains the
	 *         access, id and refresh tokens, if the user hasn't changed their
	 *         password, this will return a challenge session
	 */
	public AdminInitiateAuthResponse signInWithPassword(String username, String password) {
		Map<String, String> authParams = new HashMap<>();
		authParams.put("USERNAME", username);
		authParams.put("PASSWORD", password);

		var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder().authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
				.authParameters(authParams).userPoolId(this.userPoolId).clientId(this.clientId).build();

		return cognitoIdentityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);
	}

	/**
	 * Changed password of a user, this method needs to be called to be able to sign
	 * in with pools that have a force_change_password policy
	 *
	 * @param username         The user's username or email
	 * @param newPassword      The user's new password
	 * @param challengeSession The session received by the sign in method
	 * 
	 * @return An object with all the response data
	 */
	public AdminRespondToAuthChallengeResponse changePassword(String username, String newPassword, String challengeSession) {
		Map<String, String> authParams = new HashMap<>();
		authParams.put("USERNAME", username);
		authParams.put("NEW_PASSWORD", newPassword);

		var adminRespondToAuthChallengeRequest = AdminRespondToAuthChallengeRequest.builder()
				.challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED).challengeResponses(authParams).userPoolId(userPoolId)
				.session(challengeSession).clientId(this.clientId).build();

		return this.cognitoIdentityProviderClient.adminRespondToAuthChallenge(adminRespondToAuthChallengeRequest);
	}

	/**
	 * Changes a password user
	 *
	 * @param username    the username or email
	 * @param newPassword the new password
	 * 
	 * @return the response object without any information
	 */
	public AdminSetUserPasswordResponse resetPassword(String username, String newPassword) {
		var adminSetUserPasswordRequest = AdminSetUserPasswordRequest.builder().username(username).password(newPassword)
				.userPoolId(this.userPoolId).permanent(true).build();

		return this.cognitoIdentityProviderClient.adminSetUserPassword(adminSetUserPasswordRequest);
	}

	/**
	 * Refresh a user token, when an id token expires, this method should be called
	 * to refresh it
	 *
	 * @param refreshToken The user's refresh token
	 * 
	 * @return An object containing id and access tokens
	 */
	public AdminInitiateAuthResponse refreshToken(String refreshToken) {
		Map<String, String> authParams = new HashMap<>();
		authParams.put("REFRESH_TOKEN", refreshToken);

		var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder().authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
				.authParameters(authParams).userPoolId(this.userPoolId).clientId(this.clientId).build();

		return this.cognitoIdentityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);
	}
}
