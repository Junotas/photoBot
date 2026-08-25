package com.photobot.discord;

import com.photobot.config.AppProperties;
import com.photobot.service.CurrentWeekService;
import com.photobot.service.PhotoStorageService;
import com.photobot.service.ProcessedMessageTracker;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhotoListener extends ListenerAdapter {

  private final CurrentWeekService currentWeekService;
  private final PhotoStorageService photoStorageService;
  private final ProcessedMessageTracker processedMessageTracker;
  private final long contestChannelId;

  public PhotoListener(
      CurrentWeekService currentWeekService,
      PhotoStorageService photoStorageService,
      ProcessedMessageTracker processedMessageTracker,
      AppProperties props) {
    this.currentWeekService = currentWeekService;
    this.photoStorageService = photoStorageService;
    this.processedMessageTracker = processedMessageTracker;
    this.contestChannelId = props.channelId();
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getChannel().getIdLong() != contestChannelId) {
      return;
    }

    processedMessageTracker.markProcessed(event.getMessageIdLong());

    if (event.getAuthor().isBot()) {
      return;
    }

    List<Attachment> attachments = event.getMessage().getAttachments();
    if (attachments.isEmpty()) {
      return;
    }

    String rawName = AuthorNameResolver.resolve(event.getMessage());
    String author = DisplayNameCleaner.clean(rawName);

    photoStorageService.saveAll(currentWeekService.get(), author, attachments);
  }
}
