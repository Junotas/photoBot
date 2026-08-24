package com.photobot.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WeekCalculatorServiceTest {

  private final WeekCalculatorService calculator = new WeekCalculatorService();

  @ParameterizedTest
  @CsvSource({
    "2026-08-23, 2026-w35",
    "2026-08-24, 2026-w35",
    "2026-08-29, 2026-w35",
    "2026-08-30, 2026-w36",
    "2026-09-05, 2026-w36",
    "2026-09-06, 2026-w37",
    "2026-12-19, 2026-w51",
    "2026-12-27, 2026-w53",
    "2027-01-02, 2026-w53",
  })
  void weekIdFor_returnsExpectedWeek(String date, String expectedWeekId) {
    assertThat(calculator.weekIdFor(LocalDate.parse(date))).isEqualTo(expectedWeekId);
  }

  @ParameterizedTest
  @CsvSource({
    "2026-08-02, 2026-w32",
    "2026-08-09, 2026-w33",
    "2026-08-16, 2026-w34",
  })
  void weekIdFor_matchesHistoricalWeeksTsStartDates(String date, String expectedWeekId) {
    assertThat(calculator.weekIdFor(LocalDate.parse(date))).isEqualTo(expectedWeekId);
  }
}
