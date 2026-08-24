package com.photobot.service;

import com.photobot.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class CurrentWeekService {

  private final AtomicReference<String> currentWeek;

  public CurrentWeekService(AppProperties props) {
    this.currentWeek = new AtomicReference<>(props.currentWeek());
  }

  public String get() {
    return currentWeek.get();
  }

  public void set(String weekId) {
    currentWeek.set(weekId);
  }
}
