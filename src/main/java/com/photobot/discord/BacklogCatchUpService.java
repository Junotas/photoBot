package com.photobot.discord;

import com.photobot.config.AppProperties;
import com.photobot.service.PhotoStorageService;
import com.photobot.service.ProcessedMessageTracker;
import com.photobot.service.WeekCalculatorService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BacklogCatchUpService {

  private static final Logger log = LoggerFactory.getLogger(BacklogCatchUpService.class);
  private static final int PAGE_SIZE = 100;

  private final JDA jda;
  private final AppProperties props;
  private final ProcessedMessageTracker tracker;
  private final PhotoStorageService photoStorageService;
  private final WeekCalculatorService weekCalculatorService;

  public BacklogCatchUpService(
      JDA jda,
      AppProperties props,
      ProcessedMessageTracker tracker,
      PhotoStorageService photoStorageService,
      WeekCalculatorService weekCalculatorService) {
    this.jda = jda;
    this.props = props;
    this.tracker = tracker;
    this.photoStorageService = photoStorageService;
    this.weekCalculatorService = weekCalculatorService;
  }

  @PostConstruct
  public void catchUpOnStartup() {
    MessageChannel channel = jda.getChannelById(MessageChannel.class, props.channelId());
    if (channel == null) {
      log.warn("Configured channel {} not found - skipping backlog catch-up", props.channelId());
      return;
    }

    Long lastProcessed = tracker.lastProcessedId().orElse(null);
    if (lastProcessed == null) {
      establishBaseline(channel);
      return;
    }

    long newestSeen = lastProcessed;
    long afterId = lastProcessed;
    int totalFetched = 0;

    while (true) {
      List<Message> batch =
          channel
              .getHistoryAfter(Long.toString(afterId), PAGE_SIZE)
              .complete()
              .getRetrievedHistory();

      if (batch.isEmpty()) {
        break;
      }

      // JDA returns newest-first; process oldest-first for sane ordering.
      // SequencedCollection.reversed() (Java 21) instead of manual index juggling.
      for (Message message : batch.reversed()) {
        processBacklogMessage(message);
        if (message.getIdLong() > newestSeen) {
          newestSeen = message.getIdLong();
        }
      }

      totalFetched += batch.size();
      afterId = newestSeen;

      if (batch.size() < PAGE_SIZE) {
        break;
      }
    }

    if (newestSeen > lastProcessed) {
      tracker.markProcessed(newestSeen);
    }

    log.info("Backlog catch-up complete: {} message(s) reviewed", totalFetched);
  }

  private void establishBaseline(MessageChannel channel) {
    List<Message> latest = channel.getHistory().retrievePast(1).complete();
    if (!latest.isEmpty()) {
      tracker.markProcessed(latest.getFirst().getIdLong());
      log.info("No prior state found - baseline set, no historical backlog processed");
    }
  }

  private void processBacklogMessage(Message message) {
    if (message.getAuthor().isBot()) {
      return;
    }

    List<Attachment> attachments = message.getAttachments();
    if (attachments.isEmpty()) {
      return;
    }

    String rawName = AuthorNameResolver.resolve(message);
    String author = DisplayNameCleaner.clean(rawName);

    LocalDate postedDate = message.getTimeCreated().toLocalDate();
    String weekId = weekCalculatorService.weekIdFor(postedDate);

    photoStorageService.saveAll(weekId, author, attachments);
  }
}
