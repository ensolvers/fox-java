import com.ensolvers.fox.alerts.SlackService;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class SlackServiceTest {

  private static final String MOCK_TOKEN = "897fsdgsdljkhfno8y43";
  private static final String DEFAULT_CHANNEL_ID = "Default Channel ID";
  private final String TEST_MESSAGE = "This is the default testing message";
  private final String MESSAGE_COLOR = "#CCCCCC";
  private static SlackService slackService;

  @BeforeAll
  static void shouldCreateSlackService() {
    slackService = new SlackService(MOCK_TOKEN, DEFAULT_CHANNEL_ID);

    Assertions.assertFalse(slackService.toString().isEmpty());
  }

  @Test
  void shouldSendMessage() throws SlackApiException, IOException {
    ChatPostMessageResponse msgResponse = slackService.sendMessage(DEFAULT_CHANNEL_ID, TEST_MESSAGE);

    Assertions.assertTrue(msgResponse.isOk());
    Assertions.assertEquals(TEST_MESSAGE, msgResponse.getMessage().getText());
  }

  @Test
  void shouldSendMessageWithColor() throws SlackApiException, IOException {
    ChatPostMessageResponse msgResponse = slackService.sendMessageWithColor(TEST_MESSAGE, MESSAGE_COLOR);

    Assertions.assertTrue(msgResponse.isOk());
    Assertions.assertEquals(TEST_MESSAGE, msgResponse.getMessage().getText());
    Assertions.assertEquals(DEFAULT_CHANNEL_ID, msgResponse.getChannel());
  }

  @Test
  void shouldSendMessageToDefaultChannel () throws SlackApiException, IOException {
    ChatPostMessageResponse msgResponse = slackService.sendMessageToDefaultChannel(TEST_MESSAGE);

    Assertions.assertTrue(msgResponse.isOk());
    Assertions.assertEquals(TEST_MESSAGE, msgResponse.getMessage().getText());
    Assertions.assertEquals(DEFAULT_CHANNEL_ID, msgResponse.getChannel());
  }
}
