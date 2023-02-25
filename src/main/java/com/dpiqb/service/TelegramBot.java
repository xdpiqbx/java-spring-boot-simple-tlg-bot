package com.dpiqb.service;

import com.dpiqb.config.BotConfig;
import com.dpiqb.model.User;
import com.dpiqb.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
  private static final String HELP_TEXT = "This is help";
  private final BotConfig botConfig;
  private final UserRepository userRepository;

  public TelegramBot(BotConfig botConfig, UserRepository userRepository) {
    super(botConfig.getToken());
    this.botConfig = botConfig;
    this.userRepository = userRepository;
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
          registerUser(update.getMessage());
//          System.out.println(update.getMessage());
          startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
          break;
        case "/help":
          sendMessage(chatId, HELP_TEXT);
          break;
        default: sendMessage(chatId, "Sorry, command was not recognized");
      }
    }
  }

  private void registerUser(Message message) {
    if(userRepository.findById(message.getChatId()).isEmpty()){
      Long chatId = message.getChatId();
      Chat chat = message.getChat();

      User user = new User();
      user.setChatId(chatId);
      user.setFirstName(chat.getFirstName());
      user.setLastName(chat.getLastName());
      user.setUserName(chat.getUserName());
      user.setRegisterAt(new Timestamp(System.currentTimeMillis()));

      userRepository.save(user);
      log.info("User saved : " + user);
    }
  }

  private void startCommandReceived(long chatId, String name){
//    String answer = "Hello, "+name+", nice to meet you!";
    String answer = EmojiParser.parseToUnicode("Hello, "+name+", nice to meet you! " + "😊");
    log.info("Replied to user " + name);
    sendMessage(chatId, answer);
  }
  private void sendMessage(long chatId, String messageToSend){
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(String.valueOf(chatId));
    sendMessage.setText(messageToSend);

    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

      List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
          row1.add("Weather");
          row1.add("Get random joke");
        KeyboardRow row2 = new KeyboardRow();
          row2.add("Register");
          row2.add("Check my data");
          row2.add("Delete my data");
      keyboardRows.add(row1);
      keyboardRows.add(row2);

    replyKeyboardMarkup.setKeyboard(keyboardRows);
    sendMessage.setReplyMarkup(replyKeyboardMarkup);



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
