package com.dpiqb.config;

import com.dpiqb.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BotInitializer {
  private final TelegramBot bot;
  @EventListener({ContextRefreshedEvent.class})
  public void init() throws TelegramApiException{
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    try {
      telegramBotsApi.registerBot(bot);
    }catch (TelegramApiException e){
      log.error("Error occured:" + e.getMessage());
    }
  }
}
