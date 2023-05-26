package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.config.TelegramProperties;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.model.PublishContext;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class CallbackQueryHandler implements UpdateHandler {
    private static final String CATEGORY_ROOT = "category_root";

    private final TelegramProperties properties;
    private final KnowledgeRepository repository;
    private final Map<Long, PublishContext> readyChatToPublishMap;

    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.CALLBACK_QUERY;
    }

    @Override
    public List<BotApiMethod<? extends Serializable>> handle(Update update) {
        final CallbackQuery query = update.getCallbackQuery();
        if (query.getData().startsWith("category_")) {
            if (CATEGORY_ROOT.equals(query.getData())) {
                final List<Category> categories = properties.getMenu().getCategories();
                final AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(query.getId());
                final EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
                edit.setChatId(query.getMessage().getChatId());
                edit.setMessageId(query.getMessage().getMessageId());
                edit.setReplyMarkup(new InlineKeyboardMarkup(convertCategoriesToKeyboard(categories, List.of())));
                return List.of(answerCallbackQuery, edit);
            } else {
                final List<Integer> indexes = getIndexes(query.getData());
                final Category category = getByIndexes(indexes);
                if (category.isTerminal()) {
                    final PublishContext publishContext = readyChatToPublishMap.get(query.getFrom().getId());
                    if (publishContext != null) {
                        publishContext.setCategory(category);
                        final AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(query.getId());
                        final EditMessageText edit = new EditMessageText();
                        edit.setChatId(query.getMessage().getChatId());
                        edit.setMessageId(query.getMessage().getMessageId());
                        edit.setText(String.format("Вы выбрали категорию %s, ваша публикация будет размещена в этой категории. Пожалуйста, напишите Ваш текст ниже\n", category.getName()));
                        edit.setReplyMarkup(new InlineKeyboardMarkup(List.of()));
                        return List.of(answerCallbackQuery, edit);
                    } else {
                        final AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(query.getId());
                        final List<Knowledge> knowledge = switch (category.getSort()) {
                            case BY_LIKES -> repository.getBestByCategory(category);
                            case BY_TIME -> repository.getNewestByCategory(category);
                        };
                        if (knowledge.size() >= properties.getCountToShow()) {
                            final List<List<InlineKeyboardButton>> keyboard = knowledge.stream()
                                .map(k -> InlineKeyboardButton.builder()
                                    .callbackData("show_" + k.getId())
                                    .text(k.getPreviewText())
                                    .build()
                                )
                                .map(List::of)
                                .toList();

                            return List.of(
                                answerCallbackQuery,
                                SendMessage.builder()
                                    .chatId(query.getMessage().getChatId())
                                    .text("Выберите интересующий вас пост")
                                    .replyMarkup(new InlineKeyboardMarkup(keyboard))
                                    .build()
                            );
                        } else {
                            final List<SendMessage> messages = knowledge.stream()
                                .map(k -> k.toMessage(query.getFrom().getId()))
                                .peek(m -> m.setChatId(query.getMessage().getChatId()))
                                .toList();
                            final List<BotApiMethod<? extends Serializable>> methods = new ArrayList<>();
                            methods.add(answerCallbackQuery);
                            methods.addAll(messages);
                            return methods;
                        }
                    }
                } else {
                    final List<Category> categories = category.getCategories();
                    final List<List<InlineKeyboardButton>> keyboard = convertCategoriesToKeyboard(categories, indexes);
                    final String prevData = indexes.size() == 1 ?
                        CATEGORY_ROOT :
                        indexes.stream()
                            .limit(indexes.size() - 1)
                            .map(Object::toString)
                            .collect(Collectors.joining("_", "category_", ""));
                    keyboard.add(List.of(
                        InlineKeyboardButton.builder()
                            .text("<<<")
                            .callbackData(prevData)
                            .build()
                    ));
                    final AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(query.getId());
                    final EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
                    edit.setChatId(query.getMessage().getChatId());
                    edit.setMessageId(query.getMessage().getMessageId());
                    edit.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
                    return List.of(answerCallbackQuery, edit);
                }
            }
        } else if (query.getData().startsWith("show_")) {
            final Knowledge knowledge = repository.getById(getKnowledgeId(query.getData()));
            final SendMessage message = knowledge.toMessage(query.getFrom().getId());
            message.setChatId(query.getMessage().getChatId());
            return List.of(
                new AnswerCallbackQuery(query.getId()),
                message
            );
        } else if (query.getData().startsWith("like_")) {
            final Knowledge knowledge = repository.getById(getKnowledgeId(query.getData()));
            final Long userId = query.getFrom().getId();
            final Set<Long> usersAlreadyLikeKnowledge = knowledge.getUsersAlreadyLikeKnowledge();
            if (usersAlreadyLikeKnowledge.contains(userId)) {
                knowledge.setLikes(knowledge.getLikes() - 1L);
                usersAlreadyLikeKnowledge.remove(userId);
            } else {
                knowledge.setLikes(knowledge.getLikes() + 1L);
                usersAlreadyLikeKnowledge.add(userId);
            }
            repository.save(knowledge);

            return List.of(
                new AnswerCallbackQuery(query.getId()),
                EditMessageReplyMarkup.builder()
                    .chatId(query.getMessage().getChatId())
                    .messageId(query.getMessage().getMessageId())
                    .replyMarkup(new InlineKeyboardMarkup(List.of(knowledge.createKeyboard(userId))))
                    .build()
            );
        } else if (query.getData().startsWith("edit_")) {
            final String id = getKnowledgeId(query.getData());
            final PublishContext context = new PublishContext();
            context.setId(id);
            readyChatToPublishMap.put(query.getFrom().getId(), context);
            return List.of(
                new AnswerCallbackQuery(query.getId()),
                SendMessage.builder()
                    .chatId(query.getMessage().getChatId())
                    .text("Введите новый текст поста")
                    .build()
            );
        }
        return List.of();
    }

    private List<List<InlineKeyboardButton>> convertCategoriesToKeyboard(List<Category> categories, List<Integer> indexes) {
        final String basePath = indexes.size() == 0 ?
            "category" :
            indexes.stream()
                .map(Object::toString)
                .collect(Collectors.joining("_", "category_", ""));
        return IntStream.range(0, categories.size())
            .mapToObj(i -> InlineKeyboardButton.builder()
                .text(categories.get(i).getName())
                .callbackData(basePath + "_" + i)
                .build()
            )
            .map(List::of)
            .collect(Collectors.toList());
    }

    private List<Integer> getIndexes(String data) {
        return Arrays.stream(data.split("_"))
            .skip(1)
            .map(Integer::parseInt)
            .toList();
    }

    private Category getByIndexes(List<Integer> indexes) {
        Category result = properties.getMenu().getCategories().get(indexes.get(0));
        List<Category> subCategories = result.getCategories();
        for (int i = 1; i < indexes.size(); i++) {
            Integer index = indexes.get(i);
            result = subCategories.get(index);
            subCategories = result.getCategories();
        }
        return result;
    }

    private String getKnowledgeId(String data) {
        return data.substring(data.indexOf('_') + 1);
    }
}
