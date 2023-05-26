package ru.bsc.newteam4.telegrambot.model;

import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Knowledge {

    private String id;
    private Long authorId;
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

    public SendMessage toMessage() {
        final SendMessage message = new SendMessage();
        message.setText(getText());
        message.setEntities(getMessageEntities());
        message.setReplyMarkup(new InlineKeyboardMarkup(List.of(
            List.of(
                InlineKeyboardButton.builder()
                    .callbackData("like_" + getId())
                    .text(getLikes() + " ❤️")
                    .build(),
                InlineKeyboardButton.builder()
                    .callbackData("discuss_" + getId())
                    .text("\uD83D\uDCAC")
                    .build(),
                InlineKeyboardButton.builder()
                    .callbackData("edit_" + getId())
                    .text("✏️")
                    .build()
            )
        )));
        return message;
    }
}
