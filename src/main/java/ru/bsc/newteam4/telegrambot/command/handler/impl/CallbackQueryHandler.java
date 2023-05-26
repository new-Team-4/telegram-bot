package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Menu;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class CallbackQueryHandler implements UpdateHandler {
    private final Menu menu;

    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.CALLBACK_QUERY;
    }

    @Override
    public List<BotApiMethod<? extends Serializable>> handle(Update update) {
        final CallbackQuery query = update.getCallbackQuery();
        if (query.getData().startsWith("category_")) {
            final Category category = getByIndexes(getIndexes(query.getData()));
            if (category.isTerminal()) {
                final SendMessage message = new SendMessage();
                message.setChatId(update.getCallbackQuery().getMessage().getChatId());
                message.setText("???");
                return List.of(message);
            } else {
                final List<Category> categories = category.getCategories();
                final List<List<InlineKeyboardButton>> keyboard = IntStream.range(0, categories.size())
                    .mapToObj(i -> InlineKeyboardButton.builder()
                        .text(categories.get(i).getName())
                        .callbackData(query.getData() + "_" + i)
                        .build()
                    )
                    .map(List::of)
                    .toList();
                final AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(query.getId());
                final EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
                edit.setChatId(query.getMessage().getChatId());
                edit.setMessageId(query.getMessage().getMessageId());
                edit.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
                return List.of(answerCallbackQuery, edit);
            }
        }
        return List.of();
    }

    @Override
    public void handleException(Update update, Exception exception) {
        log.error("Error handle update: {}", update, exception);
    }

    private List<Integer> getIndexes(String data) {
        return Arrays.stream(data.split("_"))
            .skip(1)
            .map(Integer::parseInt)
            .toList();
    }

    private Category getByIndexes(List<Integer> indexes) {
        Category result = menu.getCategories().get(indexes.get(0));
        List<Category> subCategories = result.getCategories();
        for (int i = 1; i < indexes.size(); i++) {
            Integer index = indexes.get(i);
            result = subCategories.get(index);
            subCategories = result.getCategories();
        }
        return result;
    }
}