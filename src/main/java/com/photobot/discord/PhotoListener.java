package com.photobot.discord;

import com.photobot.config.AppProperties;
import com.photobot.service.CurrentWeekService;
import com.photobot.service.PhotoStorageService;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhotoListener extends ListenerAdapter {

  private final CurrentWeekService currentWeekService;
  private final PhotoStorageService photoStorageService;
  private final long contestChannelId;

  public PhotoListener(
      CurrentWeekService currentWeekService,
      PhotoStorageService photoStorageService,
      AppProperties props) {
    this.currentWeekService = currentWeekService;
    this.photoStorageService = photoStorageService;
    this.contestChannelId = props.channelId();
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getChannel().getIdLong() != contestChannelId) {
      return;
    }
    if (event.getAuthor().isBot()) {
      return;
    }

    List<Attachment> attachments = event.getMessage().getAttachments();
    if (attachments.isEmpty()) {
      return;
    }

    String author =
        event.getMember() != null
            ? event.getMember().getEffectiveName()
            : event.getAuthor().getName();

    photoStorageService.saveAll(currentWeekService.get(), author, attachments);
  }
}
