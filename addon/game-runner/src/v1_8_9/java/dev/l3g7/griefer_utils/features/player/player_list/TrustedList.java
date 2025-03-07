/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.player_list;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;

import static net.minecraft.util.EnumChatFormatting.GREEN;

@Singleton
public class TrustedList extends PlayerList {

	public TrustedList() {
		super("Trusted MM-Liste", "Markiert Spieler in verbvllert_s Trusted-MM-Liste.", "✰", "green_scroll", "Eigene Trusted", GREEN, 5, "§a§lTrusted", "https://data.community-radar.de/versions/v2/trusted.json");
	}

}
