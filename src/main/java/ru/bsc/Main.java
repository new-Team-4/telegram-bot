package ru.bsc;

import ru.bsc.config.ApplicationProperties;
import ru.bsc.config.loader.PropertiesLoader;

public class Main {
	public static void main(String[] args) {
		ApplicationProperties properties = PropertiesLoader.loadProperties("properties.yaml", ApplicationProperties.class);
		System.out.println(properties.getTelegramProperties().getBotName());
	}
}