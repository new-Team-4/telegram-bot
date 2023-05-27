package ru.bsc.newteam4.telegrambot.command.resolver.impl;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.command.resolver.UpdateHandlerResolver;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultUpdateHandlerResolver implements UpdateHandlerResolver {
    private final Map<UpdateCategory, UpdateHandler> handlerMap;

    public DefaultUpdateHandlerResolver(List<UpdateHandler> handlers) {
        handlerMap = handlers.stream()
            .collect(Collectors.toMap(UpdateHandler::getCategory, Function.identity()));
    }

    @Override
    public UpdateHandler resolve(Update update) {
        if (update.getMessage() != null) {
            final Message message = update.getMessage();
            if (message.isCommand()) {
                return handlerMap.get(UpdateCategory.COMMAND);
            } else {
                return handlerMap.get(UpdateCategory.PLAIN_MESSAGE);
            }
        }
        if (update.getCallbackQuery() != null) {
            return handlerMap.get(UpdateCategory.CALLBACK_QUERY);
        }
        if (update.getMyChatMember() != null) {
            return handlerMap.get(UpdateCategory.CHAT_STATUS);
        }
        if (update.getChatJoinRequest() != null) {
            return handlerMap.get(UpdateCategory.JOIN_REQUEST);
        }
        return handlerMap.get(UpdateCategory.DEFAULT);
    }
}
