package ru.bsc.newteam4.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.*;

@Data
public class Knowledge {

    private String id;
    private Long authorId;
    private Category category;
    private String text;
    private List<MessageEntity> messageEntities;
    private List<String> hashtags;
    private Set<Long> usersAlreadyLikeKnowledge = new HashSet<>();
    private LocalDateTime creationDate;
    private String discussionLink;

    public SendMessage toMessage(Long viewerId) {
        return toMessage(viewerId, true);
    }

    public SendMessage toMessage(Long viewerId, boolean withMenu) {
        final SendMessage message = new SendMessage();
        message.setText(getText());
        message.setEntities(getMessageEntities());
        if (withMenu) {
            message.setReplyMarkup(new InlineKeyboardMarkup(List.of(createKeyboard(viewerId))));
        }
        return message;
    }

    public List<InlineKeyboardButton> createKeyboard(Long viewerId) {
        final List<InlineKeyboardButton> keyboard = new ArrayList<>();
        keyboard.add(
            InlineKeyboardButton.builder()
                .callbackData("like_" + id)
                .text(usersAlreadyLikeKnowledge.size() + " ❤️")
                .build()
        );
        if (discussionLink != null) {
            keyboard.add(
                InlineKeyboardButton.builder()
                    .url(discussionLink)
                    .text("\uD83D\uDCAC")
                    .build()
            );
        }
        if (Objects.equals(authorId, viewerId)) {
            keyboard.add(
                InlineKeyboardButton.builder()
                    .callbackData("edit_" + id)
                    .text("✏️")
                    .build()
            );
            keyboard.add(
                InlineKeyboardButton.builder()
                    .callbackData("remove_" + id)
                    .text("\uD83D\uDDD1")
                    .build()
            );
        }
        return keyboard;
    }

    @JsonIgnore
    public String getPreviewText() {
        final int maxLength = 30;
        final String firstRow = text.split("\n")[0];
        if (firstRow.length() <= maxLength) {
            return firstRow;
        } else {
            return firstRow.substring(0, maxLength) + "...";
        }
    }
}
