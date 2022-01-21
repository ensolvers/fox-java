package com.ensolvers.fox.secrets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

public class SecretManagerService {

    public static final Logger logger = LoggerFactory.getLogger(SecretManagerService.class);
    public static final String LOG_PREFIX = "[AWS-SECRETS]";

    private final SecretsManagerClient secretsClient;

    public SecretManagerService(SecretsManagerClient secretsClient) {
        this.secretsClient = secretsClient;
    }

    /**
     * Creates a secret
     *
     * @param secretName  the name of the secret
     * @param secretValue the values of the secret
     * @return the secret's arn
     */
    public String createSecret(String secretName, String secretValue, String description) {
        logger.info("{}[START] Creating new secret with name {}", LOG_PREFIX, secretName);

        CreateSecretRequest secretRequest = CreateSecretRequest.builder().name(secretName).description(description)
                .secretString(secretValue).build();
        CreateSecretResponse secretResponse = secretsClient.createSecret(secretRequest);

        logger.info("{}[END] Creating new secret with name {}", LOG_PREFIX, secretName);

        return secretResponse.arn();
    }

    /**
     * Gets the value of a secret
     *
     * @param secretName the name/arn of the secret
     * @return the value of the secret
     */
    public String getValue(String secretName) {
        logger.info("{}[START] Retrieving secret from {}", LOG_PREFIX, secretName);

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
        GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);

        logger.info("{}[END] Retrieving secret from {}", LOG_PREFIX, secretName);

        return valueResponse.secretString();
    }
}
