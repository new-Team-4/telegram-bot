package ru.bsc.newteam4.telegrambot.config.loader;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

public class PropertiesLoader {

	private static final ClassLoader CLASS_LOADER = PropertiesLoader.class.getClassLoader();

	public static <T> T loadProperties(String fileName, Class<T> clazz) {
		T properties;
		try (final InputStream stream = CLASS_LOADER.getResourceAsStream(fileName)) {
			properties = new Yaml(new Constructor(clazz, new LoaderOptions())).load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}
}
