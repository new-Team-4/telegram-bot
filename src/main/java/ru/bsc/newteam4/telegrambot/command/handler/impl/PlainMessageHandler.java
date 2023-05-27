package ru.bsc.newteam4.telegrambot.command.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.bsc.newteam4.telegrambot.SendMessageWithCallback;
import ru.bsc.newteam4.telegrambot.command.UpdateCategory;
import ru.bsc.newteam4.telegrambot.command.handler.UpdateHandler;
import ru.bsc.newteam4.telegrambot.config.TelegramProperties;
import ru.bsc.newteam4.telegrambot.model.*;
import ru.bsc.newteam4.telegrambot.model.UserInfo;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.io.Serializable;
import java.util.*;
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
    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
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
            knowledge.setAuthor(extractAuthorInfo(update));
            knowledge.setStatus(KnowledgeStatus.PUBLISHED);
            if (message.getPhoto() != null && message.getPhoto().size() > 0) {
                final PhotoSize photo = message.getPhoto()
                    .stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(message.getPhoto().get(0));

                knowledge.setText(message.getCaption());
                knowledge.setImageId(photo.getFileId());
                knowledge.setMessageEntities(message.getCaptionEntities());
            } else {
                knowledge.setText(message.getText());
                knowledge.setMessageEntities(message.getEntities());
            }
            knowledge.setHashtags(extractHashTags(message.getEntities()));
            knowledgeRepository.save(knowledge);

            final TransformContext transformContext = new TransformContext(telegramProperties.getDiscussionChannel(), userId)
                .setMessagePrefix("Новая публикация в категории: '" + knowledge.getCategory().getName() + "'\n\n")
                .setWithMenu(false);
            final PartialBotApiMethod<Message> channelMessage = knowledge.toMessage(transformContext);
            final SendMessageWithCallback sentChannelMessageWithCallback = new SendMessageWithCallback(
                channelMessage,
                (sender, postedMessage) -> {
                    final String channelId = postedMessage.getChatId().toString().substring(4);
                    final Integer channelMessageId = postedMessage.getMessageId();
                    knowledge.setDiscussionLink("https://t.me/c/" + channelId + "/" + channelMessageId);
                    knowledgeRepository.save(knowledge);
                    readyChatToPublishMap.remove(chatId);

                    try {
                        final PartialBotApiMethod<Message> sendMessage = knowledge.toMessage(new TransformContext(
                            message.getChatId(),
                            userId
                        ));
                        if (sendMessage instanceof SendMessage send) {
                            sender.execute(send);
                        } else if (sendMessage instanceof SendPhoto photo) {
                            sender.execute(photo);
                        } else {
                            log.warn("Unknown action type: {}", sendMessage);
                        }
                    } catch (TelegramApiException e) {
                        log.error("Unable show post after publish", e);
                    }
                }
            );

            return List.of(sentChannelMessageWithCallback);
        } else {
            List<PartialBotApiMethod<? extends Serializable>> methods = new ArrayList<>();
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
                final List<PartialBotApiMethod<Message>> messages = knowledges.stream()
                    .map(k -> k.toMessage(new TransformContext(
                        update.getMessage().getChatId(),
                        update.getMessage().getFrom().getId()
                    )))
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

    private UserInfo extractAuthorInfo(Update update) {
        final User user = update.getMessage().getFrom();
        final UserInfo author = new UserInfo();
        author.setId(user.getId());
        author.setName(user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : ""));
        return author;
    }
}
