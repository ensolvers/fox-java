
import com.ensolvers.fox.cloudwatch.CloudwatchService;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import org.apache.commons.configuration.ConfigurationException;


class CloudWatchServiceTest {
  private static String ACCESS_KEY_ID;
  private static String SECRET_ACCESS_KEY_ID;
  private static Region REGION = Region.US_EAST_1;
  private static String NAMESPACE;

  private static CloudwatchService cloudwatchService;

  @BeforeAll
  public static void initialize() throws ConfigurationException {
    PropertiesConfiguration configuration = new PropertiesConfiguration();
    configuration.load("cloudwatch.properties");

    ACCESS_KEY_ID = configuration.getString("aws.access.key.id");
    SECRET_ACCESS_KEY_ID = configuration.getString("aws.access.key.secret");
    NAMESPACE = configuration.getString("aws.namespace");

  cloudwatchService =
            new CloudwatchService(
                    ACCESS_KEY_ID,
                    SECRET_ACCESS_KEY_ID,
                    REGION,
                    NAMESPACE);
  }

  @Test
  void shouldPutValueWithDefaultUnit() {
    try {
      cloudwatchService.put(
              "Test Dimension",
              "Valuable Dim",
              150);
    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  void shouldPutSeconds() {
    try {
      cloudwatchService.putSeconds(
              "Test Dimension",
              "Valuable Dim",
              "Second Metric",
              150);
    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  void shouldPutMilliseconds() {
    try {
      cloudwatchService.putMilliseconds(
              "Test Dimension",
              "Valuable Dim",
              "MilliSec Metric",
              150);
    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  void shouldPutCount() {
    try {
      cloudwatchService.putCount(
              "Dim Name",
              "Valor",
              "Metric Name",
              230L);
    } catch (Exception e) {
      Assertions.fail();
    }
  }
}

