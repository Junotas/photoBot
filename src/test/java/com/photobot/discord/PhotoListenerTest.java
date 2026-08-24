package com.photobot.discord;

import com.photobot.config.AppProperties;
import com.photobot.service.CurrentWeekService;
import com.photobot.service.PhotoStorageService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PhotoListenerTest {

  private final PhotoListener listener =
      new PhotoListener(
          mock(CurrentWeekService.class),
          mock(PhotoStorageService.class),
          new AppProperties("token", 1L, Path.of("test-photos"), "2026-w35"));

  @ParameterizedTest
  @CsvSource({
    "'Nyek [calhub|ghbgDir]', Nyek",
    "G'fudi, G'fudi",
    "'Groovy joe', 'Groovy joe'",
    "'Someone(alt)', Someone",
    "PlainName, PlainName",
    "'Trailing space before bracket [tag]', 'Trailing space before bracket'",
  })
  void cleanDisplayName_stripsBracketedTagSuffix(String raw, String expected) {
    assertThat(listener.cleanDisplayName(raw)).isEqualTo(expected);
  }
}
