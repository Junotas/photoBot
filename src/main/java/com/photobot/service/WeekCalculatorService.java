package com.photobot.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class WeekCalculatorService {

  private static final LocalDate ANCHOR_DATE = LocalDate.of(2026, 8, 23);
  private static final int ANCHOR_WEEK_NUMBER = 35;
  private static final int ANCHOR_YEAR = 2026;

  public String currentWeekId() {
    return weekIdFor(LocalDate.now());
  }

  public String weekIdFor(LocalDate date) {
    long daysSinceAnchor = ChronoUnit.DAYS.between(ANCHOR_DATE, date);
    long weeksSinceAnchor = Math.floorDiv(daysSinceAnchor, 7);
    int weekNumber = (int) (ANCHOR_WEEK_NUMBER + weeksSinceAnchor);

    return "%d-w%02d".formatted(ANCHOR_YEAR, weekNumber);
  }
}
