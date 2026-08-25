package com.photobot.service;

import com.photobot.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProcessedMessageTracker {

  private static final Logger log = LoggerFactory.getLogger(ProcessedMessageTracker.class);

  private final Path stateFile;
  private final AtomicReference<Long> lastProcessedId = new AtomicReference<>();

  public ProcessedMessageTracker(AppProperties props) {
    this.stateFile = props.stateFile();
    loadFromDisk();
  }

  public Optional<Long> lastProcessedId() {
    return Optional.ofNullable(lastProcessedId.get());
  }

  public synchronized void markProcessed(long messageId) {
    Long current = lastProcessedId.get();
    if (current != null && messageId <= current) {
      return;
    }
    lastProcessedId.set(messageId);
    persistToDisk(messageId);
  }

  private void loadFromDisk() {
    try {
      if (Files.exists(stateFile)) {
        String content = Files.readString(stateFile).trim();
        if (!content.isEmpty()) {
          lastProcessedId.set(Long.parseLong(content));
        }
      }
    } catch (IOException | NumberFormatException e) {
      log.warn("Could not read state file {}, starting with no known baseline", stateFile, e);
    }
  }

  private void persistToDisk(long messageId) {
    try {
      if (stateFile.getParent() != null) {
        Files.createDirectories(stateFile.getParent());
      }
      Files.writeString(stateFile, Long.toString(messageId));
    } catch (IOException e) {
      log.error("Failed to persist state file {}", stateFile, e);
    }
  }
}
