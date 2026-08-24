package com.photobot.config;

import com.photobot.discord.PhotoListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class DiscordConfig {

  @Bean
  public JDA jda(AppProperties props, PhotoListener photoListener) throws InterruptedException {
    JDA jda =
        JDABuilder.createDefault(props.discordToken())
            .enableIntents(EnumSet.of(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES))
            .addEventListeners(photoListener)
            .build();

    jda.awaitReady();
    return jda;
  }
}
