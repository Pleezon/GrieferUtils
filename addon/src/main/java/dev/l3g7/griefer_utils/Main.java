/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.event.AnnotationEventHandler;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.BugReporter;
import dev.l3g7.griefer_utils.settings.MainPage;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.texture.DynamicModTexture;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The main class.
 */
public class Main extends LabyModAddon {

	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	public Main() {
		instance = this;
	}

	@Override
	public void onEnable() {
		System.out.println("GrieferUtils enabling");
		long begin = System.currentTimeMillis();

		FileProvider.getClassesWithSuperClass(Feature.class).forEach(meta -> {
			if (meta.isAbstract())
				return;

			Feature instance = FileProvider.getSingleton(meta.load());
			try {
				instance.init();
			} catch (RuntimeException t) {
				MinecraftUtil.mc().displayCrashReport(new CrashReport("GrieferUtils konnte nicht geladen werden!", t));
			}
		});

		try {
			EventRegisterer.init();
			EventRegisterer.registerBugReporter(BugReporter::reportError);
			AnnotationEventHandler.init();
			AnnotationEventHandler.triggerEvent(OnEnable.class);
		} catch (Throwable t) {
			MinecraftUtil.mc().displayCrashReport(new CrashReport("GrieferUtils konnte nicht geladen werden!", t));
		}

		Map<String, DynamicModTexture> map = LabyMod.getInstance().getDynamicTextureManager().getResourceLocations();
		map.put("griefer_utils_icon", new DynamicModTexture(new ResourceLocation("griefer_utils/icons/icon.png"), "griefer_utils_icon"));

		System.out.println("GrieferUtils enabled! (took " + (System.currentTimeMillis() - begin) + " ms)");
	}

	/**
	 * Ensures GrieferUtils is shown in the {@link LabyModAddonsGui}.
	 */
	@EventListener
	private static void onGuiOpen(GuiOpenEvent<LabyModAddonsGui> event) {
		UUID uuid = Main.getInstance().about.uuid;
		for (AddonInfo addonInfo : AddonInfoManager.getInstance().getAddonInfoList())
			if (addonInfo.getUuid().equals(uuid))
				return;

		for (AddonInfo offlineAddon : AddonLoader.getOfflineAddons()) {
			if (offlineAddon.getUuid().equals(uuid)) {
				AddonInfoManager.getInstance().getAddonInfoList().add(offlineAddon);
				return;
			}
		}

		throw new RuntimeException("GrieferUtils couldn't be loaded");
	}

	@Override
	public void loadConfig() {}

	@Override
	protected void fillSettings(List<SettingsElement> list) {
		list.addAll(MainPage.settings);
	}

}
