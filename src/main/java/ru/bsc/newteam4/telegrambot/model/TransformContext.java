package ru.bsc.newteam4.telegrambot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class TransformContext {
    private final Long chatId;
    private final Long viewerId;
    private String messagePrefix;
    private boolean withMenu = true;
}
