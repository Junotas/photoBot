package com.photobot.web;

import com.photobot.service.CurrentWeekService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/week")
public class WeekController {

    private final CurrentWeekService currentWeekService;

    public WeekController(CurrentWeekService currentWeekService) {
        this.currentWeekService = currentWeekService;
    }

    @GetMapping
    public CurrentWeekResponse getCurrentWeek() {
        return new CurrentWeekResponse(currentWeekService.get());
    }

    @PutMapping
    public CurrentWeekResponse setCurrentWeek(@RequestBody WeekUpdateRequest request) {
        currentWeekService.set(request.weekId());
        return new CurrentWeekResponse(currentWeekService.get());
    }

    public record WeekUpdateRequest(String weekId) {}

    public record CurrentWeekResponse(String weekId) {}
}
