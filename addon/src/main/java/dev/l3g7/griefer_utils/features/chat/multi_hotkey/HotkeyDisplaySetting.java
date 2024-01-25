/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.multi_hotkey;

import com.google.common.collect.ImmutableSet;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.elements.*;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class HotkeyDisplaySetting extends ListEntrySetting {

	private final IconStorage iconStorage = new IconStorage();

	public final StringSetting name;
	public final KeySetting keys;
	public final StringListSetting commands;
	public final ItemSetting citybuild;

	private String defaultName;
	private Set<Integer> defaultKeys;
	private List<String> defaultCommands;
	private ItemStack defaultCitybuild;

	private int amountsTriggered = 0;

	public HotkeyDisplaySetting(String name, Set<Integer> keys, List<String> commands, ItemStack citybuild) {
		super(true, true, false);
		container = FileProvider.getSingleton(MultiHotkey.class).getMainElement();

		this.name = new StringSetting()
			.name("Name")
			.description("Wie dieser Hotkey heißen soll.")
			.icon(Material.BOOK_AND_QUILL)
			.callback(title -> {
				if (title.trim().isEmpty())
					title = "Unbenannter Hotkey";

				HeaderSetting titleSetting = (HeaderSetting) getSubSettings().getElements().get(2);
				titleSetting.name("§e§l" + title);
			});

		this.commands = new StringListSetting()
			.name("Befehl hinzufügen")
			.defaultValue(defaultCommands = commands)
			.setContainer(this);

		this.citybuild = new ItemSetting(ItemUtil.CB_ITEMS, false)
			.name("Citybuild")
			.description("Auf welchem Citybuild dieser Hotkey funktionieren soll.")
			.defaultValue((defaultCitybuild = citybuild) == null ? ItemUtil.CB_ITEMS.get(0) : defaultCitybuild);

		this.keys = new KeySetting()
			.name("Taste")
			.description("Durch das Drücken welcher Taste/-n dieser Hotkey ausgelöst werden soll.")
			.defaultValue(defaultKeys = keys)
			.icon("key")
			.pressCallback(b -> {
				if (!b || !FileProvider.getSingleton(MultiHotkey.class).isEnabled())
					return;

				String cb = this.citybuild.get().getDisplayName();
				if (!cb.equals("Egal") && !cb.equals(MinecraftUtil.getServerFromScoreboard()))
					return;

				if (this.commands.get().size() == 0) {
					displayAchievement("§cFehler", "§cBitte füge den Eintrag neu hinzu.");
					return;
				}

				String command = this.commands.get().get(amountsTriggered %= this.commands.get().size());
				if (!MessageEvent.MessageSendEvent.post(command))
					player().sendChatMessage(command);

				amountsTriggered++;
			});


		subSettings(this.name, this.keys, this.commands, this.citybuild, new HeaderSetting("§e§lBefehle").scale(0.7));
		this.name.defaultValue(defaultName = name);
		this.commands.initList();
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	public void openSettings() {
		defaultName = name.get();
		defaultKeys = keys.get();
		defaultCommands = new ArrayList<>(commands.get());
		defaultCitybuild = citybuild.get();
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
			if (!name.get().isEmpty() && !keys.get().isEmpty() && !commands.get().isEmpty() && citybuild.get() != null) {
				onChange();
				return;
			}

			if (defaultName.isEmpty()) {
				remove();
				return;
			}

			if (name.get().isEmpty())
				name.set(defaultName);
			if (keys.get().isEmpty())
				keys.set(defaultKeys);
			if (commands.get().isEmpty())
				commands.set(defaultCommands);
			if (citybuild.get() == null)
				citybuild.set(defaultCitybuild);

			onChange();
		}, this));
	}

	protected void remove() {
		keys.set(ImmutableSet.of());
		super.remove();
	}

	protected void onChange() {
		icon(citybuild.get());
		FileProvider.getSingleton(MultiHotkey.class).onChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		String subtitle = String.format("§e[%s] §f§o➡ %s", Util.formatKeys(keys.get()), commands.get().size() + (commands.get().size() == 1 ? " Befehl" : " Befehle"));

		String trimmedName = drawUtils().trimStringToWidth(name.get(), maxX - x - 25 - 48);
		String trimmedSubtitle = drawUtils().trimStringToWidth(subtitle, maxX - x - 25 - 48);
		drawUtils().drawString(trimmedName + (trimmedName.equals(name.get()) ? "" : "…"), x + 25, y + 7 - 5);
		drawUtils().drawString(trimmedSubtitle + (trimmedSubtitle.equals(subtitle) ? "" : "…"), x + 25, y + 7 + 5);
	}

}
