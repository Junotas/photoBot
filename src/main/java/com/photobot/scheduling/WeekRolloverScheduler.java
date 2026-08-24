package com.photobot.scheduling;

import com.photobot.service.CurrentWeekService;
import com.photobot.service.WeekCalculatorService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeekRolloverScheduler {

  private static final Logger log = LoggerFactory.getLogger(WeekRolloverScheduler.class);

  private final CurrentWeekService currentWeekService;
  private final WeekCalculatorService weekCalculatorService;

  public WeekRolloverScheduler(
      CurrentWeekService currentWeekService, WeekCalculatorService weekCalculatorService) {
    this.currentWeekService = currentWeekService;
    this.weekCalculatorService = weekCalculatorService;
  }

  @PostConstruct
  public void applyOnStartup() {
    applyComputedWeek();
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void rolloverIfNeeded() {
    applyComputedWeek();
  }

  private void applyComputedWeek() {
    String computed = weekCalculatorService.currentWeekId();
    String previous = currentWeekService.get();
    if (!computed.equals(previous)) {
      log.info("Rolling active week: {} -> {}", previous, computed);
      currentWeekService.set(computed);
    }
  }
}
