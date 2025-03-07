/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.player_list;

import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.core.XboxProfileResolver;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.ITextureObject;

import java.io.IOException;

import static dev.l3g7.griefer_utils.core.settings.player_list.PlayerListEntryResolver.LOOKUP_MAP;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class PlayerListEntry {


	public static final PlayerListEntry INVALID_PLAYER = new PlayerListEntry();

	public static PlayerListEntry getEntry(String name) {
		return getEntry(name, null);
	}

	public static PlayerListEntry getEntry(String name, Consumer<String> idConsumer) {
		if (!Constants.UNFORMATTED_PLAYER_NAME_PATTERN.matcher(name).matches())
			return INVALID_PLAYER;

		return LOOKUP_MAP.computeIfAbsent(name, k -> {
			PlayerListEntry e = new PlayerListEntry(name, null);
			e.idConsumer = idConsumer;
			return e;
		});
	}

	public String name;
	private String id; // The uuid / xuid of the Player
	public boolean oldSkin;
	public ITextureObject skin = null;
	public boolean loaded = false; // Whether the entry's name and id were loaded
	protected boolean exists = true;

	private Consumer<String> idConsumer;

	private PlayerListEntry() {
		exists = false;
	}

	public PlayerListEntry(String name, String id) {
		this.name = name;
		this.id = id;
		load();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		if (idConsumer != null)
			idConsumer.accept(id);
	}

	public boolean isMojang() {
		return id == null ? !name.startsWith("!") : id.contains("-");
	}

	private void load() {
		if (!isMojang()) {
			new Thread(() -> {
				if (!exists || !XboxProfileResolver.isAvailable())
					PlayerListEntryResolver.loadFromPlayerDB(this);
				if (exists) {
					try {
						PlayerListEntryResolver.loadFromXbox(this);
					} catch (IOException e) {
						PlayerListEntryResolver.loadFromPlayerDB(this);
					}
				}
			}).start();
			return;
		}

		// Try to load the uuid it from tab list
		if (mc().getNetHandler() != null) {
			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
				if (info.getGameProfile().getName().equals(name)) {
					id = info.getGameProfile().getId().toString();
				}
			}
		}

		new Thread(() -> {
			try {
				PlayerListEntryResolver.loadFromMojang(this);
			} catch (IOException e1) {
				try {
					PlayerListEntryResolver.loadFromAshcon(this);
				} catch (IOException e2) {
					e1.printStackTrace();
					e2.printStackTrace();
				}
			}
		}).start();
	}


}
