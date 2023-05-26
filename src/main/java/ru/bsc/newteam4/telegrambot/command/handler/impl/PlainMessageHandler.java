package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.config.TelegramProperties;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.model.PublishContext;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class PlainMessageHandler implements UpdateHandler {

    private final Map<Long, PublishContext> readyChatToPublishMap;
    private final KnowledgeRepository knowledgeRepository;
    private final TelegramProperties telegramProperties;

    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.PLAIN_MESSAGE;
    }

    @Override
    public List<BotApiMethod<? extends Serializable>> handle(Update update) {
        final Message message = update.getMessage();
        final Long chatId = message.getChatId();
        final PublishContext context = readyChatToPublishMap.get(chatId);
        if (context != null) {
            final Long userId = message.getFrom().getId();
            final Knowledge knowledge = context.getId() != null ?
                knowledgeRepository.getById(context.getId()) :
                new Knowledge();
            if (context.getCategory() != null) {
                knowledge.setCategory(context.getCategory());
            }
            knowledge.setAuthorId(userId);
            knowledge.setText(message.getText());
            knowledge.setMessageEntities(message.getEntities());
            knowledge.setHashtags(extractHashTags(message.getEntities()));
            knowledgeRepository.save(knowledge);
            readyChatToPublishMap.remove(chatId);

            final SendMessage sendMessage = knowledge.toMessage(userId);
            sendMessage.setChatId(message.getChatId());
            return List.of(sendMessage);
        } else {
            List<BotApiMethod<? extends Serializable>> methods = new ArrayList<>();
            final List<String> words = Arrays.asList(update.getMessage().getText().split(" "));
            final List<Knowledge> knowledges;
            final String searchType;
            if (words.stream().allMatch(w -> w.startsWith("#"))) {
                knowledges = knowledgeRepository.searchByHashtag(words);
                searchType = "хэштэгам";
            } else {
                knowledges = knowledgeRepository.searchByKeywords(words);
                searchType = "ключевым словам";
            }
            if (knowledges.size() >= telegramProperties.getCountToShow()) {
                final List<List<InlineKeyboardButton>> keyboard = knowledges.stream()
                    .map(k -> InlineKeyboardButton.builder()
                        .callbackData("show_" + k.getId())
                        .text(k.getPreviewText())
                        .build()
                    )
                    .map(List::of)
                    .toList();
                return List.of(
                    SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text("Вот все найденные посты по " + searchType)
                        .replyMarkup(new InlineKeyboardMarkup(keyboard))
                        .build()
                );
            } else {
                final List<SendMessage> messages = knowledges.stream()
                    .map(k -> k.toMessage(update.getMessage().getFrom().getId()))
                    .peek(m -> m.setChatId(update.getMessage().getChatId()))
                    .toList();
                if (messages.isEmpty()) {
                    methods = List.of(
                        SendMessage.builder()
                            .chatId(update.getMessage().getChatId())
                            .text("Ничего не найдено ¯\\_(ツ)_/¯")
                            .build()
                    );
                } else {
                    methods.addAll(messages);
                }
                return methods;
            }
        }
    }

    private List<String> extractHashTags(List<MessageEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
            .filter(messageEntity -> EntityType.HASHTAG.equals(messageEntity.getType()))
            .map(MessageEntity::getText)
            .collect(Collectors.toList());
    }
}
