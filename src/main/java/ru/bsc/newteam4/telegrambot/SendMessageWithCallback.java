package ru.bsc.newteam4.telegrambot;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@RequiredArgsConstructor
public class SendMessageWithCallback extends BotApiMethodWithCallback<Message> {
    private final SendMessage sendMessage;
    private final TelegramBotCallback<Message> callback;

    @Override
    public BotApiMethod<Message> getSource() {
        return sendMessage;
    }

    @Override
    public void callback(AbsSender sender, Message value) {
        callback.invoke(sender, value);
    }

    @Override
    public Message deserializeResponse(String answer) {
        return null;
    }

    @Override
    public String getMethod() {
        return null;
    }
}
