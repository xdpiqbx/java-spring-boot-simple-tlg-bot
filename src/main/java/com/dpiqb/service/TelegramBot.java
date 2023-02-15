package com.dpiqb.service;

import com.dpiqb.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
  private static final String HELP_TEXT = "This is help";
  private final BotConfig botConfig;

  public TelegramBot(BotConfig botConfig) {
    super(botConfig.getToken());
    this.botConfig = botConfig;
    List<BotCommand> listOfCommands = new ArrayList<>();
    listOfCommands.add(new BotCommand("/start", "Start dialog"));
    listOfCommands.add(new BotCommand("/mydata", "Get your data"));
    listOfCommands.add(new BotCommand("/deletedata", "Delete your data"));
    listOfCommands.add(new BotCommand("/help", "Info how to use this bot"));
    listOfCommands.add(new BotCommand("/settings", "Set your preferences"));
    try{
      this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
    } catch (TelegramApiException e) {
      log.error("Error setting bot's command list: " + e.getMessage());
    }
  }

  @Override
  public void onUpdateReceived(Update update) {
    if(update.hasMessage() && update.getMessage().hasText()){
      String messageText = update.getMessage().getText();
      long chatId = update.getMessage().getChatId();
      switch (messageText){
        case "/start":
          startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
          break;
        case "/help":
          sendMessage(chatId, HELP_TEXT);
          break;
        default: sendMessage(chatId, "Sorry, command was not recognized");
      }
    }
  }
  private void startCommandReceived(long chatId, String name){
    String answer = "Hello, "+name+", nice to meet you!";
    log.info("Replied to user " + name);
    sendMessage(chatId, answer);
  }
  private void sendMessage(long chatId, String messageToSend){
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(String.valueOf(chatId));
    sendMessage.setText(messageToSend);
    try {
      execute(sendMessage);
    }catch (TelegramApiException e){
      log.error("Error occured:" + e.getMessage());
    }
  }

  @Override
  public String getBotUsername() {
    return botConfig.getBotName();
  }
}
