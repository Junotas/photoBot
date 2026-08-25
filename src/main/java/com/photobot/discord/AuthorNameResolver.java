package com.photobot.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public final class AuthorNameResolver {

  private AuthorNameResolver() {}

  public static String resolve(Message message) {
    Member member = message.getMember();

    if (member == null && message.isFromGuild()) {
      try {
        member = message.getGuild().retrieveMember(message.getAuthor()).complete();
      } catch (RuntimeException e) {
        member = null;
      }
    }

    return member != null ? member.getEffectiveName() : message.getAuthor().getName();
  }
}
