package ru.bsc.newteam4.telegrambot.repository.impl;

import lombok.RequiredArgsConstructor;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;
import ru.bsc.newteam4.telegrambot.storage.Storage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;

@RequiredArgsConstructor
public class OnceKnowledgeRepository implements KnowledgeRepository {

    private final Storage<Knowledge> storage;

    @Override
    public List<Knowledge> searchByHashtag(String hashtag) {
        return storage.getAll()
            .stream()
            .filter(knowledge -> knowledge.getHashtags().stream().anyMatch(ht -> ht.equals(hashtag)))
            .collect(Collectors.toList());
    }

    @Override
    public List<Knowledge> searchByKeywords(String keywords) {
        final List<String> keywordsAsArray = List.of(keywords.split(" "));
        return storage.getAll()
            .stream()
            .filter(knowledge -> keywordsAsArray.stream().anyMatch(kw -> knowledge.getText().contains(kw)))
            .collect(Collectors.toList());
    }

    @Override
    public List<Knowledge> getBestByCategory(Category category) {
        return getByCategory(category).stream()
            .sorted(comparingLong(Knowledge::getLikes))
            .collect(Collectors.toList());
    }

    @Override
    public List<Knowledge> getNewestByCategory(Category category) {
        return getByCategory(category).stream()
            .sorted(comparing(Knowledge::getCreationDate))
            .collect(Collectors.toList());
    }

    private List<Knowledge> getByCategory(Category category) {
        return storage.getAll()
            .stream()
            .filter(knowledge -> category.equals(knowledge.getCategory()))
            .collect(Collectors.toList());
    }

    @Override
    public void save(Knowledge value) {
        value.setId(UUID.randomUUID().toString());
        value.setCreationDate(LocalDateTime.now());
        storage.save(value);
    }
}
