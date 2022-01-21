package com.ensolvers.fox.secrets;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Testcontainers
class SecretManagerServiceTest {

    @Container
    public LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
            .withServices(LocalStackContainer.Service.SECRETSMANAGER);

    @Test
    void testSecretsManager() {
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SECRETSMANAGER))
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .build();

        SecretManagerService secretManagerService = new SecretManagerService(secretsClient);

        String secretARN = secretManagerService.createSecret("test-secret", "foo:bar", "");
        assertNotNull(secretARN);

        String secretValue = secretManagerService.getValue(secretARN);
        assertNotNull(secretValue);
    }
}