package com.photobot.service;

import com.photobot.config.AppProperties;
import net.dv8tion.jda.api.entities.Message.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Service
public class PhotoStorageService {

  private static final Logger log = LoggerFactory.getLogger(PhotoStorageService.class);

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");
  private static final Pattern UNSAFE_FILENAME_CHARS = Pattern.compile("[^a-zA-Z0-9_'-]");

  // Java 21 virtual threads: one per download task, no pool sizing to tune.
  // Fine here since each task just blocks on network I/O, which is exactly
  // the case virtual threads are designed for.
  private final ExecutorService downloadExecutor = Executors.newVirtualThreadPerTaskExecutor();

  private final AppProperties props;

  public PhotoStorageService(AppProperties props) {
    this.props = props;
  }

  /**
   * Downloads and saves every image attachment on a message, in parallel. Non-image attachments
   * (e.g. someone accidentally drops a .zip) are silently skipped.
   */
  public void saveAll(String weekId, String authorName, List<Attachment> attachments) {
    List<Attachment> images = attachments.stream().filter(this::isImage).toList();

    if (images.isEmpty()) {
      log.debug("No image attachments from {} - skipping", authorName);
      return;
    }

    // SequencedCollection (Java 21): .getFirst() reads cleanly as "the
    // first one" without the classic .get(0) index noise.
    Attachment first = images.getFirst();
    log.info(
        "Processing {} image(s) from {} (first: {})",
        images.size(),
        authorName,
        first.getFileName());

    List<CompletableFuture<Void>> downloads =
        images.stream()
            .map(
                attachment ->
                    CompletableFuture.runAsync(
                        () -> saveOne(weekId, authorName, attachment), downloadExecutor))
            .toList();

    CompletableFuture.allOf(downloads.toArray(CompletableFuture[]::new)).join();
  }

  boolean isImage(Attachment attachment) {
    String ext = extensionOf(attachment.getFileName()).toLowerCase();
    return ALLOWED_EXTENSIONS.contains(ext);
  }

  private void saveOne(String weekId, String authorName, Attachment attachment) {
    String ext = extensionOf(attachment.getFileName());
    String safeAuthor = sanitize(authorName);
    Path targetDir = props.photosRoot().resolve(weekId);
    Path target = targetDir.resolve(safeAuthor + "." + ext);

    try {
      Files.createDirectories(targetDir);
      try (InputStream in = attachment.getProxy().download().get()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
      log.info("Saved {} -> {}", attachment.getFileName(), target);
    } catch (IOException | ExecutionException e) {
      log.error("Failed to save attachment {} for {}", attachment.getFileName(), authorName, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted while saving attachment for {}", authorName, e);
    }
  }

  String extensionOf(String filename) {
    int dot = filename.lastIndexOf('.');
    return dot == -1 ? "" : filename.substring(dot + 1);
  }

  String sanitize(String name) {
    return UNSAFE_FILENAME_CHARS.matcher(name).replaceAll("");
  }
}
