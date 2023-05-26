package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.model.PublishContext;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class PlainMessageHandler implements UpdateHandler {

    private final Map<Long, PublishContext> readyChatToPublishMap;
    private final KnowledgeRepository knowledgeRepository;
    private final Pattern HASH_TAG_REGEX = Pattern.compile("(#\\w+)");

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
            knowledge.setHashtags(extractHashTag(text));
            knowledgeRepository.save(knowledge);

            final SendMessage sendMessage = new SendMessage();
            sendMessage.setText("Ваше сообщение опубликовано!" + knowledge);
            sendMessage.setChatId(update.getMessage().getChatId());
            return List.of(sendMessage);
        } else {
            return List.of();
        }
    }

    private List<String> extractHashTag(String text) {
        return HASH_TAG_REGEX.matcher(text)
            .results()
            .map(r -> r.group(1))
            .collect(Collectors.toList());
    }

    @Override
    public void handleException(Update update, Exception exception) {

    }
}
