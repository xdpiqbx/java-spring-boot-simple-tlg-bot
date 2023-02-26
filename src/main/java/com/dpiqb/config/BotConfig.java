package com.dpiqb.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Data
@Configuration
@EnableScheduling
@PropertySource("application.properties")
public class BotConfig {
  @Value("${bot.name}")
  String botName;
  @Value("${bot.token}")
  String token;
  @Value("${bot.owner}")
  Long ownerId;
}
