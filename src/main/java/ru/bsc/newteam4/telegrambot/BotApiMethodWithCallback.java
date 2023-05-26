package ru.bsc.newteam4.telegrambot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.Serializable;

public abstract class BotApiMethodWithCallback<T extends Serializable> extends BotApiMethod<T> {
    public abstract BotApiMethod<T> getSource();
    public abstract void callback(AbsSender sender, T value);
}
