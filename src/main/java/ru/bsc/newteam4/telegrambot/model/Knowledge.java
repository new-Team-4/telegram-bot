package ru.bsc.newteam4.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
public class Knowledge {

    private String id;
    private Long authorId;
    private Category category;
    private String text;
    private List<MessageEntity> messageEntities;
    private List<String> hashtags;
    private Long likes = 0L;
    private Set<Long> usersAlreadyLikeKnowledge = new HashSet<>();
    private LocalDateTime creationDate;

    public void copy(Knowledge knowledge) {
        this.category = knowledge.category;
        this.text = knowledge.text;
        this.hashtags = knowledge.hashtags;
    }

    public SendMessage toMessage(Long viewerId) {
        final List<InlineKeyboardButton> keyboard = new ArrayList<>();
        keyboard.add(
            InlineKeyboardButton.builder()
                .callbackData("like_" + id)
                .text(getLikes() + " ❤️")
                .build()
        );
        keyboard.add(
            InlineKeyboardButton.builder()
                .callbackData("discuss_" + id)
                .text("\uD83D\uDCAC")
                .build()
        );
        if (Objects.equals(authorId, viewerId)) {
            keyboard.add(
                InlineKeyboardButton.builder()
                    .callbackData("edit_" + id)
                    .text("✏️")
                    .build()
            );
        }

        final SendMessage message = new SendMessage();
        message.setText(getText());
        message.setEntities(getMessageEntities());
        message.setReplyMarkup(new InlineKeyboardMarkup(List.of(keyboard)));
        return message;
    }

    @JsonIgnore
    public String getPreviewText() {
        return text.substring(0, Math.min(25, text.length()));
    }
}
