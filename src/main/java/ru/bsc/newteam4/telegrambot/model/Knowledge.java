package ru.bsc.newteam4.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
public class Knowledge {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm");

    private String id;
    private UserInfo author;
    private Category category;
    private KnowledgeStatus status;
    private String text;
    private String imageId;
    private List<MessageEntity> messageEntities;
    private List<String> hashtags;
    private Set<Long> usersAlreadyLikeKnowledge = new HashSet<>();
    private LocalDateTime creationDate;
    private LocalDateTime editDateTime;
    private String discussionLink;

    public PartialBotApiMethod<Message> toMessage(TransformContext context) {
        if (imageId != null) {
            final SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(context.getChatId());
            sendPhoto.setCaption(text);
            sendPhoto.setPhoto(new InputFile(imageId));
            sendPhoto.setCaptionEntities(messageEntities);
            if (context.isWithMenu()) {
                sendPhoto.setReplyMarkup(new InlineKeyboardMarkup(List.of(createKeyboard(context.getViewerId()))));
            }
            return sendPhoto;
        } else {
            final SendMessage message = new SendMessage();
            message.setChatId(context.getChatId());
            message.setText(text + "\n\nОпубликовано " + author.getName() + " " + creationDate.format(DATE_TIME_FORMATTER));
            message.setEntities(messageEntities);
            if (context.isWithMenu()) {
                message.setReplyMarkup(new InlineKeyboardMarkup(List.of(createKeyboard(context.getViewerId()))));
            }
            return message;
        }
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
        if (Objects.equals(author.getId(), viewerId)) {
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
