package ru.bsc.newteam4.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class Category {
    private String name;
    private SortType sort;
    private List<Category> categories;

    @JsonIgnore
    public boolean isTerminal() {
        return categories == null || categories.isEmpty();
    }
}
