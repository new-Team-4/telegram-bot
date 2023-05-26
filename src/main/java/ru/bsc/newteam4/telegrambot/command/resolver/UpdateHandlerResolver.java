package ru.bsc.newteam4.telegrambot.command.resolver;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;

public interface UpdateHandlerResolver {
    UpdateHandler resolve(Update update);
}
