package ru.bsc.newteam4.telegrambot.model;

import lombok.Data;

import java.util.List;

@Data
public class Menu {
    private String message;
    private List<Category> categories;
}
