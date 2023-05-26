package ru.bsc;

import lombok.extern.slf4j.Slf4j;
import ru.bsc.config.ApplicationProperties;
import ru.bsc.config.loader.PropertiesLoader;

@Slf4j
public class Main {
	public static void main(String[] args) {
		ApplicationProperties properties = PropertiesLoader.loadProperties("properties.yaml", ApplicationProperties.class);
		log.info(properties.getTelegramProperties().getToken());
	}
}