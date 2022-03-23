package com.ensolvers.fox.email;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtils {
  public static String replace(String template, Map<String, String> replacements) throws MessagingException {
    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      template = template.replace(entry.getKey(), entry.getValue());
    }

    validateEmptyReplacements(template);

    return template;
  }

  public static  void validateEmptyReplacements(String body) throws MessagingException {
    Pattern pattern = Pattern.compile("[$][{]\\S+[}]");
    Matcher matcher = pattern.matcher(body);
    StringBuilder allMatches = new StringBuilder("Placeholder wasn't replaced: \n");
    while (matcher.find()) {
      allMatches.append(matcher.group()).append("\n");
    }

    throw new MessagingException(allMatches.toString());
  }
}
