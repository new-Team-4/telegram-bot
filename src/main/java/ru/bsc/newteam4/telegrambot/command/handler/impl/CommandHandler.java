package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Menu;

import java.io.Serializable;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class CommandHandler implements UpdateHandler {
    private final Menu menu;

    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.COMMAND;
    }

    @Override
    public List<BotApiMethod<? extends Serializable>> handle(Update update) {
        final String command = getCommand(update.getMessage());
        switch (command) {
            case "menu": {
                final List<Category> categories = menu.getCategories();
                final List<List<InlineKeyboardButton>> keyboard = IntStream.range(0, categories.size())
                    .mapToObj(i -> InlineKeyboardButton.builder()
                        .text(categories.get(i).getName())
                        .callbackData("category_" + i)
                        .build()
                    )
                    .map(List::of)
                    .toList();
                final SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId());
                message.setText(menu.getMessage());
                message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
                return List.of(message);
            }
            case "publish": {
                final List<Category> categories = menu.getCategories();
                final List<List<InlineKeyboardButton>> keyboard = IntStream.range(0, categories.size())
                    .mapToObj(i -> InlineKeyboardButton.builder()
                        .text(categories.get(i).getName())
                        .callbackData("publish_" + i)
                        .build()
                    )
                    .map(List::of)
                    .toList();
                final SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId());
                message.setText(menu.getMessage());
                message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
                return List.of(message);
            }
            default: {
                return List.of();
            }
        }
    }

    @Override
    public void handleException(Update update, Exception exception) {
        log.error("Error handle update {}", update, exception);
    }

    private String getCommand(Message message) {
        final String text = message.getText();
        final MessageEntity commandEntity = message.getEntities().get(0);
        final Integer offset = commandEntity.getOffset();
        final Integer length = commandEntity.getLength();
        return text.substring(offset + 1, offset + length);
    }
}
