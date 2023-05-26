package ru.bsc.newteam4.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.command.resolver.UpdateHandlerResolver;
import ru.bsc.newteam4.telegrambot.config.TelegramProperties;

import java.io.Serializable;
import java.util.List;

@Slf4j
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
    public void onRegister() {
        try {
            execute(SetMyCommands.builder()
                .scope(new BotCommandScopeDefault())
                .command(new BotCommand("help", "Помощь"))
                .command(new BotCommand("menu", "Показать разделы"))
                .command(new BotCommand("publish", "Опубликовать запись"))
                .command(new BotCommand("cancel", "Отменить операцию"))
                .build()
            );
        } catch (TelegramApiException e) {
            log.error("Error while setting my commands", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        final UpdateHandler handler = resolver.resolve(update);
        try {
            final List<PartialBotApiMethod<? extends Serializable>> methods = handler.handle(update);
            for (PartialBotApiMethod<? extends Serializable> method : methods) {
                if (method instanceof BotApiMethodWithCallback<? extends Serializable> callbackMethod) {
                    invokeWithCallback(callbackMethod);
                } else if (method instanceof BotApiMethod<? extends Serializable> m) {
                    execute(m);
                } else if (method instanceof SendPhoto photo) {
                    execute(photo);
                }
            }
        } catch (TelegramApiException e) {
            log.error("Error handle update: {}", update, e);
        }
    }

    private <T extends Serializable> void invokeWithCallback(BotApiMethodWithCallback<T> method) throws TelegramApiException {
        final PartialBotApiMethod<T> sourceMethod = method.getSource();
        if (sourceMethod instanceof BotApiMethod<T> m) {
            final T value = execute(m);
            method.callback(this, value);
        } else if (sourceMethod instanceof SendPhoto photo) {
            //noinspection unchecked
            final T value = (T) execute(photo);
            method.callback(this, value);
        }
    }
}
