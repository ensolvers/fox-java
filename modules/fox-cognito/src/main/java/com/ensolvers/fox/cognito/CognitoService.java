package com.ensolvers.fox.cognito;
import java.util.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

/**
 * Currently the Cognito Service Implementation Works only with User Pool's that
 * don't have Client's Secret Credentials set.
 */
public class CognitoService {
	private final String userPoolId;
	private final String clientId;
	private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

	public CognitoService(String userPoolId, String clientId, String accessKey, String secretKey) {
		this.userPoolId = userPoolId;
		this.clientId = clientId;

		var awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

		this.cognitoIdentityProviderClient =
						CognitoIdentityProviderClient.builder()
										.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
										.region(Region.US_EAST_1)
										.build();
	}

	/**
	 * Creates a User's Group with Role and Precedence to set User's privileges
	 *
	 * @param groupName	Group's name
	 * @param description	(Optional) Group description
	 * @param precedence If user belongs to several Groups, determines which one has permission's priority
	 */
	public CreateGroupResponse createGroup(String groupName, String description, Integer precedence) {
		CreateGroupRequest createGroupRequest =
						CreateGroupRequest.builder()
										.userPoolId(this.userPoolId)
										.groupName(groupName)
										.description(description)
										.precedence(precedence)
										.build();

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
		AdminAddUserToGroupRequest adminAddUserToGroupRequest =
						AdminAddUserToGroupRequest.builder()
										.userPoolId(this.userPoolId)
										.username(username)
										.groupName(group)
										.build();

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
		AdminCreateUserRequest.Builder request =
						AdminCreateUserRequest.builder()
										.username(username)
										.userPoolId(this.userPoolId)
										.temporaryPassword(password);

		if (!sendConfirmation) {
			request.messageAction(MessageActionType.SUPPRESS);
		}

		return cognitoIdentityProviderClient.adminCreateUser(request.build());
	}

	/**
	 * Sign in, this method is used for pools that use username/password
	 * authentication.
	 *
	 * If your Pool's Client has set a Secret Key, in order for this method to work you need to get
	 * the Cognito's User Pool Username to be able to generate right the Secret_Hash.
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

		AdminInitiateAuthRequest adminInitiateAuthRequest =
						AdminInitiateAuthRequest.builder()
										.authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
										.authParameters(authParams)
										.userPoolId(this.userPoolId)
										.clientId(this.clientId)
										.build();

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

		AdminRespondToAuthChallengeRequest adminRespondToAuthChallengeRequest =
						AdminRespondToAuthChallengeRequest.builder()
										.challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
										.challengeResponses(authParams)
										.userPoolId(userPoolId)
										.session(challengeSession)
										.clientId(this.clientId)
										.build();

		return this.cognitoIdentityProviderClient.adminRespondToAuthChallenge(adminRespondToAuthChallengeRequest);
	}

	/**
	 * Changes a User's password
	 *
	 * @param username    the username or email
	 * @param newPassword the new password
	 * 
	 * @return the response object without any information
	 */
	public AdminSetUserPasswordResponse resetPassword(String username, String newPassword) {
		AdminSetUserPasswordRequest adminSetUserPasswordRequest =
						AdminSetUserPasswordRequest.builder()
										.username(username)
										.password(newPassword)
										.userPoolId(this.userPoolId)
										.permanent(true)
										.build();

		return this.cognitoIdentityProviderClient.adminSetUserPassword(adminSetUserPasswordRequest);
	}

	/**
	 * Refresh a user token, when an id token expires, this method should be called
	 * to refresh it.
	 *
	 * @param refreshToken The user's refresh token
	 * 
	 * @return An object containing id and access tokens
	 */
	public AdminInitiateAuthResponse refreshToken(String refreshToken) {
		Map<String, String> authParams = new HashMap<>();
		authParams.put("REFRESH_TOKEN", refreshToken);

		AdminInitiateAuthRequest adminInitiateAuthRequest =
						AdminInitiateAuthRequest.builder()
										.authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
										.authParameters(authParams)
										.userPoolId(this.userPoolId)
										.clientId(this.clientId)
										.build();

		return this.cognitoIdentityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);
	}

	/**
	 * Removes a User's Group from the User Pool
	 *
	 * @param groupName Group's name to be removed
	 * @return {@link DeleteGroupResponse}
	 */
	public DeleteGroupResponse removeGroup(String groupName) {
		DeleteGroupRequest deleteGroupRequest =
						DeleteGroupRequest.builder()
										.userPoolId(this.userPoolId)
										.groupName(groupName)
										.build();

		return this.cognitoIdentityProviderClient.deleteGroup(deleteGroupRequest);
	}

	/**
	 * Sign out a User.
	 *
	 * @param username User's username to be signed out
	 * @return {@link AdminUserGlobalSignOutResponse}
	 */
	public AdminUserGlobalSignOutResponse signOut(String username) {
		AdminUserGlobalSignOutRequest adminUserGlobalSignOutRequest =
						AdminUserGlobalSignOutRequest.builder()
										.userPoolId(this.userPoolId)
										.username(username)
										.build();

		return this.cognitoIdentityProviderClient.adminUserGlobalSignOut(adminUserGlobalSignOutRequest);
	}

	/**
	 * Disables a User from the User Pool.
	 * The User could still be restored.
	 *
	 * @param username User's username
	 * @return {@link AdminDisableUserResponse}
	 */
	public AdminDisableUserResponse disableUser(String username) {
		AdminDisableUserRequest adminDisableUserRequest =
						AdminDisableUserRequest.builder()
										.userPoolId(this.userPoolId)
										.username(username)
										.build();

		return this.cognitoIdentityProviderClient.adminDisableUser(adminDisableUserRequest);
	}

	/**
	 * Delete's a user from the Pool. Once deleted it can't be restored.
	 *
	 * @param username User's username
	 * @return {@link AdminDeleteUserResponse}
	 */
	public AdminDeleteUserResponse deleteUser(String username) {
		AdminDeleteUserRequest adminDeleteUserRequest =
						AdminDeleteUserRequest.builder()
										.userPoolId(this.userPoolId)
										.username(username)
										.build();

		return this.cognitoIdentityProviderClient.adminDeleteUser(adminDeleteUserRequest);
	}
}
