package ru.bsc.newteam4.telegrambot.config;

import lombok.Data;
import ru.bsc.newteam4.telegrambot.model.Menu;

@Data
public class TelegramProperties {
    private String botName;
    private String token;
    private Long countToShow;
    private Long discussionChannel;
    private Menu menu;
}
