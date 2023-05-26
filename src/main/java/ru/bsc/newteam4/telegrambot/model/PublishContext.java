package ru.bsc.newteam4.telegrambot.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PublishContext {

    private String id;
    private Category category;

}
