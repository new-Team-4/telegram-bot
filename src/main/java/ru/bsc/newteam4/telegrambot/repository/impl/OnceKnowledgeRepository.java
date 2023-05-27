package ru.bsc.newteam4.telegrambot.repository.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import ru.bsc.newteam4.telegrambot.config.StorageProperties;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Paths.get;
import static java.util.Comparator.comparing;

@RequiredArgsConstructor
public class OnceKnowledgeRepository implements KnowledgeRepository {

    private final ObjectMapper mapper = new ObjectMapper()
        .registerModules(new JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Map<String, Knowledge> storage = new HashMap<>();
    private final StorageProperties storageProperties;

    @Override
    public void loadFromMemory() {
        try {
            final Path storageLocation = get(storageProperties.getLocation());
            if (!exists(storageLocation)) {
                createDirectory(storageLocation);
            }
            try (final DirectoryStream<Path> stream = newDirectoryStream(storageLocation, "*.json")) {
                Knowledge knowledge;
                for (final Path path : stream) {
                    knowledge = mapper.readValue(path.toFile(), Knowledge.class);
                    storage.put(knowledge.getId(), knowledge);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Knowledge getById(String id) {
        return storage.get(id);
    }

    @Override
    public List<Knowledge> searchByHashtag(List<String> hashtags) {
        return storage.values()
            .stream()
            .filter(knowledge -> knowledge.getHashtags().stream().anyMatch(hashtags::contains))
            .collect(Collectors.toList());
    }

    @Override
    public List<Knowledge> searchByKeywords(List<String> keywords) {
        return storage.values()
            .stream()
            .filter(knowledge -> keywords.stream().anyMatch(kw -> knowledge.getText().contains(kw)))
            .sorted(Comparator.comparingLong((Knowledge k) -> k.getUsersAlreadyLikeKnowledge().size()).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public List<Knowledge> getBestByCategory(Category category) {
        return getByCategory(category).stream()
            .sorted(Comparator.comparingLong((Knowledge k) -> k.getUsersAlreadyLikeKnowledge().size()).reversed())
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
        if (value.getId() == null) {
            value.setId(UUID.randomUUID().toString());
            value.setCreationDate(LocalDateTime.now());
        }
        value.setEditDateTime(LocalDateTime.now());
        storage.put(value.getId(), value);
        saveToFile(value);
    }

    @Override
    public void remove(String id) {
        storage.remove(id);
        try {
            Files.deleteIfExists(getKnowledgePath(id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void saveToFile(Knowledge knowledge) {
        try {
            mapper.writeValue(getKnowledgePath(knowledge.getId()).toFile(), knowledge);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getKnowledgePath(String id) {
        return get(storageProperties.getLocation(), id + ".json");
    }
}
