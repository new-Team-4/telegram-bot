package ru.bsc.newteam4.telegrambot.command.handler.impl;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;

import java.io.Serializable;
import java.util.List;

public class JoinRequestUpdateHandler implements UpdateHandler {
    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.JOIN_REQUEST;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
        final ChatJoinRequest joinRequest = update.getChatJoinRequest();
        return List.of(new ApproveChatJoinRequest(
            joinRequest.getChat().getId().toString(),
            joinRequest.getUser().getId()
        ));
    }
}
