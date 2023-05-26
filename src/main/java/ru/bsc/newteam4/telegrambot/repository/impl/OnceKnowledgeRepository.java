package ru.bsc.newteam4.telegrambot.repository.impl;

import lombok.RequiredArgsConstructor;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;
import ru.bsc.newteam4.telegrambot.storage.Storage;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OnceKnowledgeRepository implements KnowledgeRepository {

    private final Storage<Knowledge> storage;

    @Override
    public List<Knowledge> searchByHashtag(String hashtag) {
        return getByPredicate(knowledge -> knowledge.getHashtags().stream().anyMatch(ht -> ht.equals(hashtag)));
    }

    @Override
    public List<Knowledge> searchByKeywords(String keywords) {
        final List<String> keywordsAsArray = List.of(keywords.split(" "));
        return getByPredicate(knowledge -> keywordsAsArray.stream().anyMatch(kw -> knowledge.getText().contains(kw)));
    }

    @Override
    public List<Knowledge> getBestByCategory(Category category) {
        return getByCategory(category);
    }

    @Override
    public List<Knowledge> getNewestByCategory(Category category) {
        return null;
    }

    private List<Knowledge> getByCategory(Category category) {
        return storage.getAll()
            .stream()
            .filter(knowledge -> category.equals(knowledge.getCategory()))
            .collect(Collectors.toList());
    }

    private List<Knowledge> getByPredicate(Predicate<Knowledge> predicate) {
        return storage.getAll()
            .stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    @Override
    public void save(Knowledge value) {
        value.setId(UUID.randomUUID().toString());
        storage.save(value);
    }
}
