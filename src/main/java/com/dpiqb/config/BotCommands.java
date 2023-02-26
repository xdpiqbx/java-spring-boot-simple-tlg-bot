package com.dpiqb.config;

import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BotCommands {
  private static Map<String, String> commands(){
    Map<String, String> commands = new LinkedHashMap<>();
    commands.put("/start", "Start dialog");
    commands.put("/register", "Register?");
    commands.put("/mydata", "Get your data");
    commands.put("/deletedata", "Delete your data");
    commands.put("/help", "Info how to use this bot");
    commands.put("/settings", "Set your preferences");
    return commands;
  }
  public static List<BotCommand> listOfCommands(){
    List<BotCommand> listOfCommands = new ArrayList<>();
    BotCommands.commands().forEach((command, description) ->
        listOfCommands.add(new BotCommand(command, description)));
    return listOfCommands;
  }
}
