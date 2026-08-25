package com.photobot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    String discordToken, long channelId, Path photosRoot, String currentWeek, Path stateFile) {}
