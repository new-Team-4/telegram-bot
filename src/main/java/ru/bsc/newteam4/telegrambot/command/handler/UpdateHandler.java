package ru.bsc.newteam4.telegrambot.command.handler;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;

import java.io.Serializable;
import java.util.List;

public interface UpdateHandler {
    UpdateCategory getCategory();
    List<BotApiMethod<? extends Serializable>> handle(Update update);
    void handleException(Update update, Exception exception);
}
