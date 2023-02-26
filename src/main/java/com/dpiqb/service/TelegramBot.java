package com.dpiqb.service;

import com.dpiqb.config.BotCommands;
import com.dpiqb.config.BotConfig;
import com.dpiqb.model.Ads;
import com.dpiqb.model.AdsRepository;
import com.dpiqb.model.User;
import com.dpiqb.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
  private final AdsRepository adsRepository;
  public TelegramBot(BotConfig botConfig, UserRepository userRepository, AdsRepository adsRepository) {
    super(botConfig.getToken());
    this.botConfig = botConfig;
    this.userRepository = userRepository;
    this.adsRepository = adsRepository;
  }
  @PostConstruct
  private void postConstruct() {
    try{
      this.execute(
          new SetMyCommands(
              BotCommands.listOfCommands(),
              new BotCommandScopeDefault(),
              null
          )
      );
    } catch (TelegramApiException e) {
      log.error("Error setting bot's command list: " + e.getMessage());
    }
  }

  @Override
  public void onUpdateReceived(Update update) {
    if(update.hasMessage() && update.getMessage().hasText()){
      String messageText = update.getMessage().getText();
      long chatId = update.getMessage().getChatId();

      if(messageText.contains("/send") && botConfig.getOwnerId() == chatId){
        String command = EmojiParser.parseToUnicode(messageText.substring(0, messageText.indexOf(" ")));
        String textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));

        Iterable<User> users = userRepository.findAll();
        for (User user : users) {
          sendMessage(user.getChatId(), textToSend);
        }
        return;
      }

      switch (messageText) {
        case "/start" -> {
          registerUser(update.getMessage());
          startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
        }
        case "/help" -> prepareAndSendMessage(chatId, HELP_TEXT);
        case "/register" -> register(chatId);
        default -> prepareAndSendMessage(chatId, "Sorry, command was not recognized");
      }

    } else if (update.hasCallbackQuery()) {
      Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
      Long chatId = update.getCallbackQuery().getMessage().getChatId();

      String data = update.getCallbackQuery().getData();

      switch (Const.valueOf(data)){
        case YES_BUTTON -> {
          String text = "You press YES button";
          executeEditMessage(messageId, chatId, text);
        }
        case NO_BUTTON -> {
          String text = "You press NO button";
          executeEditMessage(messageId, chatId, text);
        }
      }

    }
  }
  private void register(long chatId) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("Do you really want to register?");

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    List <InlineKeyboardButton> buttonsRow1 = new ArrayList<>();

    InlineKeyboardButton yesButton = new InlineKeyboardButton();
      yesButton.setText("Yes");
      yesButton.setCallbackData(Const.YES_BUTTON.name());

    InlineKeyboardButton noButton = new InlineKeyboardButton();
      noButton.setText("No");
      noButton.setCallbackData(Const.NO_BUTTON.name());

    buttonsRow1.add(yesButton);
    buttonsRow1.add(noButton);

    keyboard.add(buttonsRow1);
    inlineKeyboardMarkup.setKeyboard(keyboard);
    message.setReplyMarkup(inlineKeyboardMarkup);

    executeMessage(message);
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
    prepareAndSendMessage(chatId, answer);
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

    executeMessage(sendMessage);
  }

  @Override
  public String getBotUsername() {
    return botConfig.getBotName();
  }

  private void executeEditMessage(int messageId, long chatId, String text){
    EditMessageText editMessageText = new EditMessageText();
    editMessageText.setMessageId(messageId);
    editMessageText.setChatId(chatId);
    editMessageText.setText(text);
    try {
      execute(editMessageText);
    }catch (TelegramApiException e){
      log.error("Error occured:" + e.getMessage());
    }
  }
  public void executeMessage(SendMessage message){
    try {
      execute(message);
    }catch (TelegramApiException e){
      log.error("Error occured:" + e.getMessage());
    }
  }
  @Scheduled(cron = "${cron.scheduler}")
  private void sendAds(){
    Iterable<Ads> ads = adsRepository.findAll();
    Iterable<User> users = userRepository.findAll();
    ads.forEach(ad -> {
      users.forEach(user -> {
            prepareAndSendMessage(user.getChatId(), ad.getAd());
          }
      );
    });
  }
  public void prepareAndSendMessage(long chatId, String messageToSend){
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(String.valueOf(chatId));
    sendMessage.setText(messageToSend);
    executeMessage(sendMessage);
  }
}
