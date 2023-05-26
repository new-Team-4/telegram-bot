package ru.bsc.newteam4.telegrambot;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.command.resolver.UpdateHandlerResolver;
import ru.bsc.newteam4.telegrambot.config.TelegramProperties;

import java.io.Serializable;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private final String botName;
    private final UpdateHandlerResolver resolver;

    public TelegramBot(DefaultBotOptions options, TelegramProperties properties, UpdateHandlerResolver resolver) {
        super(options, properties.getToken());
        this.botName = properties.getBotName();
        this.resolver = resolver;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final UpdateHandler handler = resolver.resolve(update);
        try {
            final List<BotApiMethod<? extends Serializable>> methods = handler.handle(update);
            for (BotApiMethod<? extends Serializable> method : methods) {
                execute(method);
            }
        } catch (TelegramApiException e) {
            handler.handleException(update, e);
        }
    }
}
