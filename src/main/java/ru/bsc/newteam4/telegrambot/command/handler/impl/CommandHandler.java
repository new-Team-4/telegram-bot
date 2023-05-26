package ru.bsc.newteam4.telegrambot.command.handler.impl;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;

import java.io.Serializable;
import java.util.List;

public class CommandHandler implements UpdateHandler {
    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.COMMAND;
    }

    @Override
    public List<BotApiMethod<? extends Serializable>> handle(Update update) {
        final String command = getCommand(update.getMessage());
        final SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Твоя команда: " + command);
        return List.of(message);
    }

    @Override
    public void handleException(Update update, Exception exception) {

    }

    private String getCommand(Message message) {
        final String text = message.getText();
        final MessageEntity commandEntity = message.getEntities().get(0);
        final Integer offset = commandEntity.getOffset();
        final Integer length = commandEntity.getLength();
        return text.substring(offset + 1, offset + length);
    }
}
