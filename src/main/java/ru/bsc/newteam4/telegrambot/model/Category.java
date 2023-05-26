package ru.bsc.newteam4.telegrambot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class Category {
    private String name;
    private SortType sort;
    private List<Category> categories;

    public boolean isTerminal() {
        return categories == null || categories.isEmpty();
    }
}
