package ru.bsc.newteam4.telegrambot.storage;

import java.util.List;

public interface Storage<T> {

    List<T> getAll();

    void save(T value);

}
