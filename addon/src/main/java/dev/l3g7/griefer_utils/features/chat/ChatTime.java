/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.utils.Material;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatException;

import static dev.l3g7.griefer_utils.core.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ChatTime extends Feature {

	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat();

	private final StringSetting style = new StringSetting()
		.name("Design")
		.description("Das Design des Prefixes, mit Unterstützung von &-Formatierungscodes.\n" +
			"%s ist die Zeit an sich.")
		.icon(Material.EMPTY_MAP)
		.setValidator(v -> {
			try {
				String.format(v, "");
				return v.contains("%s");
			} catch (IllegalFormatException e) {
				return false;
			}
		});

	private final StringSetting format = new StringSetting()
		.name("Zeitformat")
		.description("Das Format der Zeit, gemäß Javas Date Format.")
		.icon(Material.EMPTY_MAP)
		.callback(DATE_FORMAT::applyPattern)
		.setValidator(v -> {
			try {
				DATE_FORMAT.applyPattern(v);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("ChatTime")
		.description("Fügt den Zeitpunkt des Empfangens vor Chatnachrichten hinzu.")
		.icon(Material.WATCH)
		.subSettings(style, format);

	public ChatTime() {
		if(enabled.getStorage().value == null) { // If no value loaded, try loading from TebosBrime's addon
			File configFile = new File(mc().mcDataDir, "LabyMod/addons-1.8/config/ChatTime.json");
			if(configFile.exists()) {
				IOUtil.read(configFile).asJsonObject().ifPresent(obj -> {
					JsonObject cfg = obj.get("config").getAsJsonObject();
					format.defaultValue(cfg.has("chatData") ? cfg.get("chatData").getAsString() : "HH:mm:ss");
					style.defaultValue(cfg.has("chatData2") ? cfg.get("chatData2").getAsString().replace("%time%", "%s") : "&4[&e%s&4] ");
				});
			}
		}
		format.defaultValue("HH:mm:ss");
		style.defaultValue("&7[&6%s&7] ");
	}

	@EventListener(priority = LOWEST)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		String time = String.format(style.get(), DATE_FORMAT.format(new Date())).replace('&', '§') + "§r";
		event.message = new ChatComponentText(time).appendSibling(event.message);
	}

}
