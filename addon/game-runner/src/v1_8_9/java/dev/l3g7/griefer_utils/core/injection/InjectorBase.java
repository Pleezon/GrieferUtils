/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.injection.transformer.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

import java.util.HashMap;
import java.util.Map;

public class InjectorBase {

	static Config mixinConfig;
	private static final Map<String, Transformer> transformers = new HashMap<>();

	public static void initialize(String labymodNamespace, String refmap) {
		// Initialize Mixin
		MixinBootstrap.init();

		// Usage of deprecated API required for mixin 7.11 compatibility
		//noinspection deprecation
		mixinConfig = Config.create("griefer_utils.mixins.json", MixinEnvironment.getDefaultEnvironment());

		// Load refmap
		Reflection.set(mixinConfig.getConfig(), "refMapperConfig", "refmaps/" + refmap + ".json");

		// Register mixins
		Reflection.invoke(Mixins.class, "registerConfiguration", mixinConfig);
		if (labymodNamespace != null)
			mixinConfig.getConfig().decorate("labymod-namespace", labymodNamespace);

		// Load transformers
		for (ClassMeta meta : FileProvider.getClassesWithSuperClass(Transformer.class)) {
			Transformer transformer = Reflection.construct(meta.load());
			transformers.put(transformer.getTarget(), transformer);
		}

		Reflection.setMappingTarget(LabyBridge.labyBridge.activeMapping());
	}

	public boolean shouldTransform(String name, String transformedName) {
		if (name.startsWith("com.github.lunatrius.schematica"))
			Constants.SCHEMATICA = true;
		else if (name.startsWith("de.emotechat.addon"))
			Constants.EMOTECHAT = true;

		return transformers.containsKey(transformedName);
	}

	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		Transformer transformer = transformers.get(transformedName);

		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(basicClass);
		reader.accept(classNode, 0);

		transformer.transform(classNode);

		ClassWriter writer = new ClassWriter(3);
		classNode.accept(writer);
		return writer.toByteArray();
	}

}
