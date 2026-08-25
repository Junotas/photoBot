package com.photobot.service;

import com.photobot.config.AppProperties;
import net.dv8tion.jda.api.entities.Message.Attachment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PhotoStorageServiceTest {

    private final PhotoStorageService service = new PhotoStorageService(
            new AppProperties("token", 1L, Path.of("test-photos"), "2026-w35", Path.of("test-state.txt"))
    );

    @ParameterizedTest
    @CsvSource({
            "photo.png, png",
            "photo.PNG, PNG",
            "archive.tar.gz, gz",
            "noextension, ''",
    })
    void extensionOf_parsesFileExtension(String filename, String expectedExt) {
        assertThat(service.extensionOf(filename)).isEqualTo(expectedExt);
    }

    @ParameterizedTest
    @CsvSource({
            "photo.png, true",
            "photo.PNG, true",

            "photo.webp, true",
            "doc.pdf, false",
            "archive.zip, false",
    })
    void isImage_onlyAcceptsKnownImageExtensions(String filename, boolean expected) {
        Attachment attachment = mock(Attachment.class);
        when(attachment.getFileName()).thenReturn(filename);
        assertThat(service.isImage(attachment)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "'Nyek [calhub|ghbgDir]', NyekcalhubghbgDir",
            "G'fudi, G'fudi",
            "'Groovy joe', Groovyjoe",
            "'Someone(alt)', Someonealt",
            "PlainName, PlainName",
    })
    void sanitize_stripsCharactersUnsafeForFilenames(String raw, String expected) {
        assertThat(service.sanitize(raw)).isEqualTo(expected);
    }
}