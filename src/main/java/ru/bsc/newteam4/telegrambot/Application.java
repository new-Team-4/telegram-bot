package ru.bsc.newteam4.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.bsc.newteam4.telegrambot.command.handler.impl.CommandHandler;
import ru.bsc.newteam4.telegrambot.command.resolver.impl.DefaultUpdateHandlerResolver;
import ru.bsc.newteam4.telegrambot.config.ApplicationProperties;
import ru.bsc.newteam4.telegrambot.config.loader.PropertiesLoader;

import java.util.List;

@Slf4j
public class Application {
	public static void main(String[] args) {
        try {
            ApplicationProperties properties = PropertiesLoader.loadProperties("properties.yaml", ApplicationProperties.class);
            final TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            final DefaultBotOptions options = new DefaultBotOptions();
            options.setAllowedUpdates(List.of("message", "callback_query"));
            api.registerBot(new TelegramBot(options, properties.getTelegramProperties(), new DefaultUpdateHandlerResolver(List.of(
                new CommandHandler()
            ))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}