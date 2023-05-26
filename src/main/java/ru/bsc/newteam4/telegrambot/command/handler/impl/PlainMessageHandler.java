package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
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
            return List.of();
        }
    }

    @Override
    public void handleException(Update update, Exception exception) {
        log.error("Error handle update: {}", update, exception);
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
