package ru.bsc.newteam4.telegrambot.repository.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import lombok.RequiredArgsConstructor;
import ru.bsc.newteam4.telegrambot.config.StorageProperties;
import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Knowledge;
import ru.bsc.newteam4.telegrambot.repository.KnowledgeRepository;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final ObjectMapper mapper = new ObjectMapper()
        .registerModules(new JSR310Module())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Map<String, Knowledge> storage = new HashMap<>();
    private final StorageProperties storageProperties;

    @Override
    public void loadFromMemory() {
        try {
            final Path storageLocation = Paths.get(storageProperties.getLocation());
            if (!Files.exists(storageLocation)) {
                Files.createDirectory(storageLocation);
            }
            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(storageProperties.getLocation()), "*.json")) {
                for (Path path : stream) {
                    final Knowledge knowledge = mapper.readValue(path.toFile(), Knowledge.class);
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
            knowledge.setCategory(value.getCategory());
            knowledge.setText(value.getText());
            knowledge.setLikes(value.getLikes());
            knowledge.setAuthorId(value.getAuthorId());
            knowledge.setHashtags(value.getHashtags());
            knowledge.setMessageEntities(value.getMessageEntities());
            knowledge.setUsersAlreadyLikeKnowledge(value.getUsersAlreadyLikeKnowledge());
            storage.remove(value.getId());
        } else {
            value.setId(UUID.randomUUID().toString());
            value.setCreationDate(LocalDateTime.now());
        }
        storage.put(value.getId(), value);
        saveToFiles();
    }

    private synchronized void saveToFiles() {
        try {
            for (Knowledge knowledge : storage.values()) {
                final Path path = Path.of(storageProperties.getLocation(), knowledge.getId() + ".json");
                mapper.writeValue(path.toFile(), knowledge);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
