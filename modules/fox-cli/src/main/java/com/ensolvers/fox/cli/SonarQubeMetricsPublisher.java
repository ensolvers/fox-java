package com.ensolvers.fox.cli;

import com.ensolvers.fox.alerts.SlackService;
import com.ensolvers.fox.quality.SonarQubeService;
import com.ensolvers.fox.quality.model.SonarQubeMetricHistoryResponse;
import com.ensolvers.fox.quality.model.SonarQubeMetricMeasure;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import picocli.CommandLine;

/**
 * Command-line application that allows to generate and send SonarQube reports to a Slack channel
 *
 * @author José Matías Rivero (jose.matias.rivero@gmail.com)
 */
@CommandLine.Command(name = "sonarqube-metrics-report")
public class SonarQubeMetricsPublisher implements Callable<Integer> {

  public static final String GRAY = "#CCCCCC";
  public static final String RED = "#FF0000";
  public static final String GREEN = "#00FF00";

  @CommandLine.Option(
      names = {"--sonar-token"},
      description = "SonarQube token needed to interact with the API",
      required = true)
  private String sonarToken;

  @CommandLine.Option(
      names = {"--sonar-component"},
      description = "SonarQube component from which the metrics will be fetched",
      required = true)
  private String sonarComponent;

  @CommandLine.Option(
      names = {"--slack-bot-token"},
      description = "Slack bot token that will be used to publish the results",
      required = true)
  private String slackBotToken;

  @CommandLine.Option(
      names = {"--slack-channel"},
      description = "Slack channel ID in which the results will be published",
      required = true)
  private String slackChannel;

  @CommandLine.Option(
      names = {"--metrics"},
      description = "Comma-separated list of metrics to be reported",
      defaultValue = "bugs,code_smells,security_hotspots")
  private String metrics;

  @Override
  public Integer call() throws Exception {
    SlackService slackService = new SlackService(slackBotToken, slackChannel);
    SonarQubeService sonarQubeService = new SonarQubeService(sonarToken);

    Stream<String> metrics = Arrays.stream(this.metrics.split(","));

    this.sendIntroMessagge(slackService);

    metrics.forEach(
        metric -> {
          fetchAndPublishMetric(metric, slackService, sonarQubeService);
        });

    return 0;
  }

  private void sendIntroMessagge(SlackService slackService) throws Exception {
    String url =
        "https://sonarcloud.io/project/issues?id="
            + URLEncoder.encode(this.sonarComponent, "utf8")
            + "&resolved=false";
    slackService.sendMessageWithColor(
        "*SONARQUBE ANALYSIS RESULTS FOR <" + url + "|" + this.sonarComponent + ">*", "#4287f5");
  }

  private void fetchAndPublishMetric(
      String metric, SlackService slackService, SonarQubeService sonarQubeService) {
    try {
      SonarQubeMetricHistoryResponse history =
          sonarQubeService.getMetricHistory(
              this.sonarComponent, metric, Instant.now().minus(10, ChronoUnit.DAYS), Instant.now());

      String message;
      String messageColor = GRAY;

      if (!history.getMeasures().isEmpty()) {
        message = "*Measures for metric: " + metric + "*\n";

        List<SonarQubeMetricMeasure> listToPublish =
            history.getMeasures().get(0).getHistory().stream()
                // sort historic values in reverse order
                .sorted(
                    (m1, m2) ->
                        (int)
                            (m2.getDate().toInstant().toEpochMilli()
                                - m1.getDate().toInstant().toEpochMilli()))
                .limit(4)
                .collect(Collectors.toList());

        double firstValue = Double.parseDouble(listToPublish.get(0).getValue());
        double lastValue =
            Double.parseDouble(listToPublish.get(listToPublish.size() - 1).getValue());

        if (firstValue > lastValue) {
          messageColor = RED;
        } else if (firstValue < lastValue) {
          messageColor = GREEN;
        }

        message =
            listToPublish.stream()
                .map(m -> m.getDate() + ": " + m.getValue())
                .collect(Collectors.joining("\n"));
      } else {
        message = "No measures for metric: " + metric;
      }

      slackService.sendMessageWithColor(message, messageColor);
    } catch (Exception e) {
      System.out.printf("Error when trying to execute SonarQube publishing\n");
      e.printStackTrace();
    }
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new SonarQubeMetricsPublisher()).execute(args);
    System.exit(exitCode);
  }
}
