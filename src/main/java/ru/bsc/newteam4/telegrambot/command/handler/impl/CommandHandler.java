package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
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
import ru.bsc.newteam4.telegrambot.model.PublishContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class CommandHandler implements UpdateHandler {
    private final Menu menu;
    private final Map<Long, PublishContext> context;

    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.COMMAND;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
        final String command = getCommand(update.getMessage());
        switch (command) {
            case "start", "help" -> {
                return List.of(
                    SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text(menu.getHelpMessageText())
                        .parseMode(ParseMode.MARKDOWN)
                        .build()
                );
            }
            case "cancel" -> {
                final Long chatId = update.getMessage().getChatId();
                final PublishContext removed = context.remove(chatId);
                if (removed != null) {
                    return List.of(
                        SendMessage.builder()
                            .chatId(chatId)
                            .text("Операция отменена")
                            .build()
                    );
                } else {
                    return List.of(
                        SendMessage.builder()
                            .chatId(chatId)
                            .text("Нечего отменять ¯\\_(ツ)_/¯")
                            .build()
                    );
                }
            }
            case "menu" -> {
                return List.of(createMenuMessage(update, false));
            }
            case "publish" -> {
                context.put(update.getMessage().getChatId(), new PublishContext());
                return List.of(createMenuMessage(update, true));
            }
            default -> {
                return List.of();
            }
        }
    }

    private SendMessage createMenuMessage(Update update, boolean publishMenu) {
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
        message.setText(publishMenu ? menu.getPublishHeaderMessage() : menu.getMessage());
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
        return message;
    }

    private String getCommand(Message message) {
        final String text = message.getText();
        final MessageEntity commandEntity = message.getEntities().get(0);
        final Integer offset = commandEntity.getOffset();
        final Integer length = commandEntity.getLength();
        return text.substring(offset + 1, offset + length);
    }
}
