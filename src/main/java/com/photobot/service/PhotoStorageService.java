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

  private final ExecutorService downloadExecutor = Executors.newVirtualThreadPerTaskExecutor();

  private final AppProperties props;

  public PhotoStorageService(AppProperties props) {
    this.props = props;
  }


  public void saveAll(String weekId, String authorName, List<Attachment> attachments) {
    List<Attachment> images = attachments.stream().filter(this::isImage).toList();

    if (images.isEmpty()) {
      log.debug("No image attachments from {} - skipping", authorName);
      return;
    }


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

  private boolean isImage(Attachment attachment) {
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

  private String extensionOf(String filename) {
    int dot = filename.lastIndexOf('.');
    return dot == -1 ? "" : filename.substring(dot + 1);
  }

  private String sanitize(String name) {
    return UNSAFE_FILENAME_CHARS.matcher(name).replaceAll("");
  }
}
