package ru.bsc.newteam4.telegrambot.model;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Knowledge {

    private String id;
    private Category category;
    private String text;
    private List<MessageEntity> messageEntities;
    private List<String> hashtags;
    private Long likes = 0L;
    private LocalDateTime creationDate;

    public void copy(Knowledge knowledge) {
        this.category = knowledge.category;
        this.text = knowledge.text;
        this.hashtags = knowledge.hashtags;
    }
}
