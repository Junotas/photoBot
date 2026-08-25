package com.photobot.discord;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DisplayNameCleanerTest {

    @ParameterizedTest
    @CsvSource({
            "'Nyek [calhub|ghbgDir]', Nyek",
            "'Nyek [whatever] ps5', Nyek",
            "G'fudi, G'fudi",
            "'Groovy joe', 'Groovy joe'",
            "'Someone(alt)', Someone",
            "PlainName, PlainName",
    })
    void clean_stripsBracketedTagSuffix(String raw, String expected) {
        assertThat(DisplayNameCleaner.clean(raw)).isEqualTo(expected);
    }
}