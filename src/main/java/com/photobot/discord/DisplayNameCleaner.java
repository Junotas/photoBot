package com.photobot.discord;

import java.util.regex.Pattern;

public final class DisplayNameCleaner {

  private static final Pattern TAG_SUFFIX = Pattern.compile("\\s*[\\[(].*$");

  private DisplayNameCleaner() {}

  public static String clean(String rawName) {
    return TAG_SUFFIX.matcher(rawName).replaceFirst("").trim();
  }
}
