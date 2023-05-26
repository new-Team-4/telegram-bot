package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.model.PublishContext;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class PlainMessageHandler implements UpdateHandler {

    private final Map<Long, PublishContext> readyChatToPublishMap;
    private final KnowledgeRepository knowledgeRepository;

    @Override
    public UpdateCategory getCategory() {
        return UpdateCategory.PLAIN_MESSAGE;
    }

    @Override
    public List<BotApiMethod<? extends Serializable>> handle(Update update) {
        final Long chatId = update.getMessage().getChatId();
        if (readyChatToPublishMap.get(chatId) != null) {
            final String text = update.getMessage().getText();
            final PublishContext publishContext = readyChatToPublishMap.get(chatId);
            final Knowledge knowledge = new Knowledge();
            knowledge.setText(text);
            knowledge.setMessageEntities(update.getMessage().getEntities());
            knowledge.setCategory(publishContext.getCategory());
            knowledge.setHashtags(extractHashTags(text, update.getMessage().getEntities()));
            knowledgeRepository.save(knowledge);
            readyChatToPublishMap.remove(chatId);

            final SendMessage sendMessage = new SendMessage();
            sendMessage.setText("Ваше сообщение опубликовано!");
            sendMessage.setChatId(update.getMessage().getChatId());
            return List.of(sendMessage);
        } else {
            return List.of();
        }
    }

    private List<String> extractHashTags(String text, List<MessageEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
            .filter(messageEntity -> EntityType.HASHTAG.equals(messageEntity.getType()))
            .map(MessageEntity::getText)
            .collect(Collectors.toList());
    }

    @Override
    public void handleException(Update update, Exception exception) {

    }
}
