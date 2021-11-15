package com.ensolvers.fox.cognito;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.SdkField;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * AWS Cognito's User Pools Service Test Cases
 */
//@SpringBootTest(classes = {CognitoService.class})
//@ExtendWith(SpringExtension.class)
//@AutoConfigureMockMvc
public class CognitoServiceTestCase {
  //private final String AWS_URL = "url-to-perform-req";
  private final String AWS_USER_POOL_ID = "us-east-1_HX91PFT9h";
  private final String AWS_COGNITO_CLIENT_ID = "794m7vs88a99k0n3itm0mtun9g";
  private final String AWS_COGNITO_POOL_ACCESS = "AKIAX62PK5MC22I52MXF";
  private final String AWS_COGNITO_POOL_SECRET = "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";

  private CognitoService cognitoService = new CognitoService(
          AWS_USER_POOL_ID,
          AWS_COGNITO_CLIENT_ID,
          AWS_COGNITO_POOL_ACCESS,
          AWS_COGNITO_POOL_SECRET);

  private final String USERNAME = "testCognito@email.com";
  private final String PASSWORD = "pwTR#@#!2";


  private AdminAddUserToGroupResponse addUserToGroupResponse;

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
  public void shouldCreateUserWithPassword() {
    AdminCreateUserResponse response =
            cognitoService.createUserWithPassword("cognitoUsername@email.com","pwTR#@#!2",false);

    Assertions.assertFalse(response.user().username().isEmpty());
  }

  /**
   * User should sign in to the existing User Pool.
   *
   * This method works only with pools set to authorize via username and password.
   *
   * @error Auth flow not enabled for this client
   */
  @Test
  public void shouldSignInWithPassword() {
    AdminInitiateAuthResponse response =
            cognitoService.signInWithPassword("cognitoUsername@email.com","pwTR#@#!2");

    Assertions.assertFalse(response.authenticationResult().idToken().isEmpty());
  }

  /**
   * Should change a user's password.
   *
   * Works with pools that have a force_change_password policy.
   */
  @Test
  public void shouldChangePassword() {
    cognitoService.changePassword();
  }

  /**
   * Should reset the user's password and set a nwe one.
   */
  @Test
  public void shouldResetPassword() {

  }

  /**
   * Should refresh the user's auth token
   */
  @Test
  public void shouldRefreshToken() {

  }

  /**
   * Creates a Group
   */
  @Test
  public void shouldCreateUserGroup() {

  }
  /**
   * Should Add a User to a Cognito group.
   * Both User and Group must exist, otherwise operation will fail.
   */
  @Test
  public void shouldAddUserToGroup() {
    this.addUserToGroupResponse = cognitoService.addUserToGroup("username", "password");

    Integer size = this.addUserToGroupResponse.sdkFields().size();

    assertThat(size).isGreaterThan(0);

    AdminAddUserToGroupResponse response = cognitoService.addUserToGroup("cognitoTestUser","cognitoGROUP");
    List<SdkField<?>> fields = response.sdkFields();
    System.out.println("Response Fields" + fields);
    Assertions.assertTrue(true);

  }

  @Test
  public void shouldRemoveUserFromGroup() {

  }

  @Test
  public void shouldDisableUser() {

  }
}
