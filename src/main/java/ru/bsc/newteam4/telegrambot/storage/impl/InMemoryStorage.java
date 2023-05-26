package ru.bsc.newteam4.telegrambot.storage.impl;

import ru.bsc.newteam4.telegrambot.storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class InMemoryStorage<T> implements Storage<T> {

    private final List<T> storage = new ArrayList<>();

    @Override
    public List<T> getAll() {
        return storage;
    }

    @Override
    public void save(T value) {
        storage.add(value);
    }
}
