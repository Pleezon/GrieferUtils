/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.labymod.laby3.Init;
import dev.l3g7.griefer_utils.post_processor.processors.build.RefmapConverter;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * A processor applied after building.
 */
public class BuildPostProcessor {

	private static final Map<String, String> LABY_3_ADDON_JSON = Map.of(
		"uuid", "%uuid%",
		"name", "GrieferUtils",
		"description", "\uD83D\uDC4B",
		"icon", "griefer_utils_icon",
		"debug", System.getProperty("griefer_utils.debug"),
		"beta", System.getProperty("griefer_utils.beta"),
		"transformerClass", "dev.l3g7.griefer_utils.labymod.laby3.Init",
		"addonVersion", System.getProperty("griefer_utils.version")
	);

	private static FileSystem fs;

	public static void main(String[] args) throws IOException {
		String version = System.getProperty("griefer_utils.version");

		// Rename jar
		File jar = new File("build/libs/game-runner-" + version + "-1.8.9-obfuscated.jar");
		File newJar = new File("../build/libs/griefer-utils-v" + version + ".jar");
		Files.copy(jar.toPath(), newJar.toPath(), REPLACE_EXISTING);

		// Trigger patches
		try (FileSystem fs = FileSystems.newFileSystem(newJar.toPath())) {
			BuildPostProcessor.fs = fs;
			mergeAddonJson();
			processBootstrapClasses();
			RefmapConverter.convertRefmap(fs);
			cleanup();
		}
	}

	private static Path pathOf(Class<?> c) {
		return fs.getPath(c.getName().replace('.', '/') + ".class");
	}

	/**
	 * Merges the LabyMod 3 addon.json into the existing json.
	 */
	private static void mergeAddonJson() throws IOException {
		// Read
		JsonObject addon;
		try (Reader in = Files.newBufferedReader(fs.getPath("addon.json"), UTF_8)) {
			addon = Streams.parse(new JsonReader(in)).getAsJsonObject();
		}

		// Merge
		LABY_3_ADDON_JSON.forEach(addon::addProperty);

		// Write
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (OutputStream out = Files.newOutputStream(fs.getPath("addon.json"))) {
			out.write(gson.toJson(addon).getBytes(UTF_8));
		}
	}

	/**
	 * Transforms the entrypoint class and transformers.
	 */
	private static void processBootstrapClasses() throws IOException {
		process(pathOf(Init.class));
		process(pathOf(AutoUpdater.Init.class));
		try (Stream<Path> stream = Files.walk(pathOf(EarlyPostProcessor.class).getParent())) {
			stream
				.filter(Files::isRegularFile)
				.forEach(BuildPostProcessor::process);
		}
	}

	private static void process(Path path) {
		try {
			byte[] bytes = Files.readAllBytes(path);
			String name = path.toString().substring(0, path.toString().length() - 6).replace('/', '.');
			Files.write(path, EarlyPostProcessor.INSTANCE.transform(name, name, bytes));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void cleanup() {
		// delete LabyMod 4 autogen
		delete(fs.getPath("fernflower_abstract_parameter_names.txt"));

		// delete build post processors
		delete(pathOf(RefmapConverter.class).getParent());
		delete(pathOf(BuildPostProcessor.class));

		// mark other build artifacts to emphasize processed jar file
		empty(Path.of("build/libs/game-runner-" + System.getProperty("griefer_utils.version") + "-1.8.9-obfuscated.jar"));
	}

	private static void delete(Path path) {
		try {
			if (!Files.exists(path))
				return;

			if (Files.isRegularFile(path)) {
				Files.delete(path);
				return;
			}

			try (Stream<Path> walk = Files.walk(path)) {
				walk.sorted(Comparator.reverseOrder())
					.filter(Predicate.not(path::equals))
					.forEach(BuildPostProcessor::delete);
			}
			Files.delete(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void empty(Path path) {
		try {
			if (!Files.exists(path))
				return;

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			new ZipOutputStream(bout).close();
			Files.write(path, bout.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
