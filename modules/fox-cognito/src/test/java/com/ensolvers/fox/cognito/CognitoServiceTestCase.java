package com.ensolvers.fox.cognito;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.jupiter.api.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

/**
 * AWS Cognito's User Pools Service Test Cases
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CognitoServiceTestCase {
  private static String AWS_USER_POOL_ID;
  private static String AWS_COGNITO_CLIENT_SECRET;
  private static String AWS_COGNITO_CLIENT_ID;
  private static String AWS_COGNITO_POOL_ACCESS;

  private static CognitoService cognitoService;

  private final String USERNAME = "testCognito@email.com";
  private final String PASSWORD = "pwTR#@#!2";
  private final String NEW_PASSWORD = "8fKl#$ff";

  private final String GROUP_NAME = "NewAWSGroup";
  private final String GROUP_DESCRIPTION = "Goal Oriented Group";
  private final Integer GROUP_PRECEDENCE = 2;

  /**
   * In order for configuration to work properly, please create a properties file including the following entries:
   *  - aws.user.pool.id
   *  - aws.cognito.client.id
   *  - aws.cognito.client.secret
   *  - aws.access.key.id
   *
   * @throws ConfigurationException If configuration cannot be loaded
   */
  @BeforeAll
  public static void initialize() throws ConfigurationException {
    PropertiesConfiguration configuration = new PropertiesConfiguration();
    configuration.load("poolConfig.properties");

    AWS_USER_POOL_ID = configuration.getString("aws.user.pool.id");
    AWS_COGNITO_CLIENT_ID = configuration.getString("aws.cognito.client.id");
    AWS_COGNITO_CLIENT_SECRET = configuration.getString("aws.cognito.client.secret");
    AWS_COGNITO_POOL_ACCESS = configuration.getString("aws.access.key.id");

    cognitoService = new CognitoService(
            AWS_USER_POOL_ID,
            AWS_COGNITO_CLIENT_ID,
            AWS_COGNITO_POOL_ACCESS,
            AWS_COGNITO_CLIENT_SECRET);
  }


  /**
   * A new User should be created with the introduced username and password.
   * Optionally an email notification can be sent to the new user's inbox.
   *
   * Username requirements: has to be an email.
   * Password requirements: min 8 char length, symbols and numbers.
   *
   * This method works only with pools set to authorize via username and password.
   */
  @Test
  @Order(1)
  public void shouldCreateUserWithPassword() {

    AdminCreateUserResponse response =
            cognitoService.createUserWithPassword(USERNAME,PASSWORD,false);

    Assertions.assertFalse(response.user().username().isEmpty());
  }

  /**
   * User should sign in to the existing User Pool.
   *
   * This method works only with pools set to authorize via username and password.
   *
   */
  @Test
  @Order(2)
  public void shouldSignInWithPassword() {
    AdminInitiateAuthResponse response =
            cognitoService.signInWithPassword(USERNAME,PASSWORD);

    Assertions.assertTrue(response.sdkHttpResponse().isSuccessful());
  }

  /**
   * Should change a user's password.
   *
   *
   * Works with pools that have a force_change_password policy.
   */
  @Test
  @Order(3)
  public void shouldChangePassword() {
    AdminInitiateAuthResponse response =
            cognitoService.signInWithPassword(USERNAME,PASSWORD);

    System.out.println("New Session " + response.session());

    AdminRespondToAuthChallengeResponse responsee =
            cognitoService.changePassword(USERNAME, NEW_PASSWORD, response.session());

    Assertions.assertTrue(responsee.sdkHttpResponse().isSuccessful());

  }

  /**
   * Should reset the user's password and set a nwe one.
   */
  @Test
  @Order(4)
  public void shouldResetPassword() {
    AdminSetUserPasswordResponse resetPasswordResponse =
            cognitoService.resetPassword(USERNAME, NEW_PASSWORD);

    resetPasswordResponse.sdkFields().forEach(System.out::println);

    Assertions.assertTrue(resetPasswordResponse.sdkHttpResponse().isSuccessful());
  }

  /**
   * Should refresh the user's auth token
   */
  @Test
  @Order(5)
  public void shouldRefreshToken() {
    AdminInitiateAuthResponse response =
            cognitoService.signInWithPassword(USERNAME,NEW_PASSWORD);

    AdminInitiateAuthResponse refreshResponse =
            cognitoService.refreshToken(response.authenticationResult().refreshToken());

    Assertions.assertTrue(refreshResponse.sdkHttpResponse().isSuccessful());
  }

  /**
   * Creates a Group
   */
  @Test
  @Order(6)
  public void shouldCreateUserGroup() {
    CreateGroupResponse createGroupResponse =
            cognitoService.createGroup(GROUP_NAME, GROUP_DESCRIPTION, GROUP_PRECEDENCE);

    Assertions.assertFalse(createGroupResponse.sdkFields().isEmpty());
  }
  /**
   * Should Add a User to a Cognito group.
   * Both User and Group must exist, otherwise operation will fail.
   */
  @Test
  @Order(7)
  public void shouldAddUserToGroup() {
    AdminAddUserToGroupResponse addUserToGroupResponse = cognitoService.addUserToGroup(USERNAME, GROUP_NAME);

    Assertions.assertTrue(addUserToGroupResponse.sdkHttpResponse().isSuccessful());
  }

  @Test
  @Order(8)
  public void shouldRemoveUserFromGroup() {
    AdminRemoveUserFromGroupResponse removeUserFromGroupResponse =
            cognitoService.removeUserFromGroup(USERNAME, GROUP_NAME);

    Assertions.assertTrue(removeUserFromGroupResponse.sdkHttpResponse().isSuccessful());
  }

  @Test
  @Order(9)
  public void shouldRemoveGroup() {
    DeleteGroupResponse deleteGroupResponse =
            cognitoService.removeGroup(GROUP_NAME);

    Assertions.assertTrue(deleteGroupResponse.sdkHttpResponse().isSuccessful());
  }

  @Test
  @Order(10)
  public void shouldSignOutUser() {
    AdminUserGlobalSignOutResponse adminUserGlobalSignOutResponse =
            cognitoService.signOut(USERNAME);

    Assertions.assertTrue(adminUserGlobalSignOutResponse.sdkHttpResponse().isSuccessful());
  }

  @Test
  @Order(11)
  public void shouldDisableUser() {
    AdminDisableUserResponse adminDisableUserResponse =
            cognitoService.disableUser(USERNAME);

    Assertions.assertTrue(adminDisableUserResponse.sdkHttpResponse().isSuccessful());
  }

  @Test
  @Order(12)
  public void deleteUser() {
    AdminDeleteUserResponse deleteUserResponse =
            cognitoService.deleteUser(USERNAME);

    Assertions.assertTrue(deleteUserResponse.sdkHttpResponse().isSuccessful());
  }
}
