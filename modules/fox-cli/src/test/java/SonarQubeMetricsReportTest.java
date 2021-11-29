import com.ensolvers.fox.cli.SonarQubeMetricsReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//@Disabled("")
class SonarQubeMetricsReportTest {

  private final String sonarToken = "32d23d32";
  private final String sonarComponent = "23d32d23d";
  private final String slackBotToken = "23d32d23";
  private final String slackChannel = "23d23d2";

  //@Disabled("")
  @Test
  void shouldCreateReport() {
    try {
      SonarQubeMetricsReport.main(sonarToken, sonarComponent, slackBotToken, slackBotToken);
    } catch ( Exception e ) {
      Assertions.fail();
    }

    Assertions.assertTrue(true);
  }
}
