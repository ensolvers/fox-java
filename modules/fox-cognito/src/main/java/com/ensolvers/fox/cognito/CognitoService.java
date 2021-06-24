package com.ensolvers.fox.cognito;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.*;

public class CognitoService {
  private final String userPoolId;
  private final String clientId;
  private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

  public CognitoService(String userPoolId, String clientId, String accessKey, String secretKey) {
    this.userPoolId = userPoolId;
    this.clientId = clientId;

    var awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

    this.cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .region(Region.US_EAST_1)
        .build();
  }

  public AdminAddUserToGroupResponse addUserToGroup(String username, String group) {
    var adminAddUserToGroupRequest = AdminAddUserToGroupRequest.builder()
        .userPoolId(userPoolId)
        .username(username)
        .groupName(group)
        .build();

    return cognitoIdentityProviderClient.adminAddUserToGroup(adminAddUserToGroupRequest);
  }

  public AdminCreateUserResponse createUserWithPassword(String username, String password, boolean sendConfirmation) {
    var request = AdminCreateUserRequest.builder()
        .username(username)
        .userPoolId(userPoolId)
        .temporaryPassword(password);

    if (sendConfirmation) {
      request.messageAction(MessageActionType.SUPPRESS);
    }

    return cognitoIdentityProviderClient.adminCreateUser(request.build());
  }

  public AdminInitiateAuthResponse signInWithPassword(String username, String password) {
    Map<String, String> authParams = new HashMap<>();
    authParams.put("USERNAME", username);
    authParams.put("PASSWORD", password);

    var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder()
            .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
            .authParameters(authParams)
            .userPoolId(userPoolId)
            .clientId(clientId)
            .build();

    return cognitoIdentityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);
  }

  public AdminRespondToAuthChallengeResponse changePassword(String username, String newPassword, String challengeSession) {
    Map<String, String> authParams = new HashMap<>();
    authParams.put("USERNAME", username);
    authParams.put("NEW_PASSWORD", newPassword);

    var adminRespondToAuthChallengeRequest = AdminRespondToAuthChallengeRequest.builder()
        .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
        .challengeResponses(authParams)
        .userPoolId(userPoolId)
        .session(challengeSession)
        .clientId(clientId)
        .build();

    return cognitoIdentityProviderClient.adminRespondToAuthChallenge(adminRespondToAuthChallengeRequest);
  }

  public AdminInitiateAuthResponse refreshToken(String refreshToken) {
    Map<String, String> authParams = new HashMap<>();
    authParams.put("REFRESH_TOKEN", refreshToken);

    var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder()
        .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
        .authParameters(authParams)
        .userPoolId(userPoolId)
        .clientId(clientId)
        .build();

    return cognitoIdentityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);
  }
}
