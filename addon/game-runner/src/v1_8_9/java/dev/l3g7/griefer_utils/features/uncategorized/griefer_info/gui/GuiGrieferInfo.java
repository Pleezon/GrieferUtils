/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.DataHandler;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.botshops.GuiBotShops;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms.GuiFarms;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff.GuiFreestuff;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.ItemUtil.createItem;

public class GuiGrieferInfo extends GuiBigChest {

	private static final ItemStack DISCORD = ItemUtil.fromNBT("{id:\"minecraft:skull\",Count:1b,tag:{SkullOwner:{Id:\"ef9383de-e103-43dc-bab8-d4f53a791e4c\",Properties:{textures:[0:{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM5ZWU3MTU0OTc5YjNmODc3MzVhMWM4YWMwODc4MTRiNzkyOGQwNTc2YTI2OTViYTAxZWQ2MTYzMTk0MjA0NSJ9fX0=\"}]}},display:{Name:\"§9§nDiscord\",Lore:[0:\"§fAlle Daten werden von §aGriefer.Info§r bereitgestellt.\",1:\"§fKlicke hier, um auf deren Discord zu gelangen.\"]}},Damage:3s}");
	private static final ItemStack WEBSITE = ItemUtil.fromNBT("{id:\"minecraft:skull\",Count:1b,tag:{SkullOwner:{Id:\"6c55365e-8165-4525-aff9-7c65fabb0c99\",Properties:{textures:[0:{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDUxZTA0MGQxODljZjJmODhmYzBmYTNlZTMxNzA3NjMzYmJlNmYwMjM2Y2Y1ZGY1ODgwMDE1OWNlMDA3M2ZkZSJ9fX0=\"}]}},display:{Name:\"§a§nGriefer.Info\",Lore:[0:\"§fAlle Daten werden von §aGriefer.Info§r bereitgestellt.\",1:\"§fKlicke hier, um auf deren Webseite zu gelangen.\"]}},Damage:3s}");

	public static final GuiGrieferInfo GUI = new GuiGrieferInfo();

	private GuiGrieferInfo() {
		super("§2§lGriefer.info", 5);
		addItem(11, createItem(Blocks.mob_spawner, 0, "§6Farmen"), () -> {
			new GuiFarms().open();
			DataHandler.requestFarms();
		});
		addItem(13, createItem(Items.emerald, 0, "§6Botshops"), () -> {
			new GuiBotShops().open();
			DataHandler.requestBotshops();
		});
		addItem(15, createItem(Blocks.chest, 0, "§6Freestuff"), () -> {
			new GuiFreestuff().open();
			DataHandler.requestFreestuff();
		});

		addItem(30, WEBSITE, () -> labyBridge.openWebsite("https://griefer.info/site/index"));
		addItem(32, DISCORD, () -> labyBridge.openWebsite("https://discord.gg/YnPatPbVmx"));
	}

	@EventListener
	public static void onMsg(MessageEvent.MessageSendEvent event) {
		String message = event.message.trim().toLowerCase();

		if (message.equals("/gi") || message.equals("/info")) {
			event.cancel();
			TickScheduler.runAfterRenderTicks(GUI::open, 1);
		}
	}

}
