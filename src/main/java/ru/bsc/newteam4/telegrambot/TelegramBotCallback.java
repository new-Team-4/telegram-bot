package ru.bsc.newteam4.telegrambot;

import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.Serializable;

public interface TelegramBotCallback<T extends Serializable> {
    void invoke(AbsSender sender, T value);
}
