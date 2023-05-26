package ru.bsc.newteam4.telegrambot;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.config.TelegramProperties;

public class TelegramBot extends TelegramLongPollingBot {
    private final String botName;

    public TelegramBot(DefaultBotOptions options, TelegramProperties properties) {
        super(options, properties.getToken());
        this.botName = properties.getBotName();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }
}
