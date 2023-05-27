package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;

import java.io.Serializable;
import java.util.List;

@Slf4j
public class ChatStatusUpdateHandler implements UpdateHandler {
    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.CHAT_STATUS;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
        final ChatMemberUpdated myChatMember = update.getMyChatMember();
        final Chat chat = myChatMember.getChat();
        final boolean isAdminStatus = myChatMember.getNewChatMember().getStatus().equals("administrator");
        final boolean isChannel = chat.getType().equals("channel");
        if (isAdminStatus && isChannel) {
            log.info("New admin channel id: {}", chat.getId());
        }
        return List.of();
    }
}
