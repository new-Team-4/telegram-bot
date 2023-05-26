package ru.bsc.newteam4.telegrambot.repository;

import ru.bsc.newteam4.telegrambot.model.Category;
import ru.bsc.newteam4.telegrambot.model.Knowledge;

import java.util.List;

public interface KnowledgeRepository {

    void loadFromMemory();
`
    Knowledge getById(String id);

    List<Knowledge> searchByHashtag(String hashtag);

    List<Knowledge> searchByKeywords(String keywords);

    List<Knowledge> getBestByCategory(Category category);

    List<Knowledge> getNewestByCategory(Category category);

    void save(Knowledge value);

}
