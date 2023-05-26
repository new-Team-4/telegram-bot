package ru.bsc.newteam4.telegrambot.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;

@RequiredArgsConstructor
public class OnceKnowledgeRepository implements KnowledgeRepository {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Knowledge> storage = new HashMap<>();

    @Override
    public void loadFromMemory() {

    }

    @Override
    public List<Knowledge> searchByHashtag(String hashtag) {
        return storage.values()
            .stream()
            .filter(knowledge -> knowledge.getHashtags().stream().anyMatch(ht -> ht.equals(hashtag)))
            .collect(Collectors.toList());
    }

    @Override
    public List<Knowledge> searchByKeywords(String keywords) {
        final List<String> keywordsAsArray = List.of(keywords.split(" "));
        return storage.values()
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
        return storage.values()
            .stream()
            .filter(knowledge -> category.equals(knowledge.getCategory()))
            .collect(Collectors.toList());
    }

    @Override
    public void save(Knowledge value) {
        if (value.getId() != null && storage.get(value.getId()) != null) {
            final Knowledge knowledge = storage.get(value.getId());
            knowledge.copy(value);

        } else {
            value.setId(UUID.randomUUID().toString());
            value.setCreationDate(LocalDateTime.now());
        }
        storage.put(value.getId(), value);
        saveToFiles();
    }

    private synchronized void saveToFiles() {

    }
}
