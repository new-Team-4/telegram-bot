package ru.bsc.newteam4.telegrambot.command.handler.impl;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;

import java.io.Serializable;
import java.util.List;

public class DefaultHandler implements UpdateHandler {

    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.DEFAULT;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
        return List.of(
            SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Пожалуйста, введите команду /help, потому что я не понимаю что вы от меня хотите \uD83D\uDE1C")
                .build()
        );
    }
}
