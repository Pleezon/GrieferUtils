/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import net.labymod.core.asm.LabyModCoreMod;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class ServerCheck {

	private static boolean onGrieferGames;
	private static boolean onCitybuild;

	public static boolean isOnGrieferGames() {
		return onGrieferGames || (!LabyModCoreMod.isObfuscated() && world() != null);
	}

	public static boolean isOnCitybuild() {
		return onCitybuild || (!LabyModCoreMod.isObfuscated() && world() != null);
	}

	@EventListener(priority = Priority.HIGHEST)
	public void onPacketReceive(ServerEvent.GrieferGamesJoinEvent event) {
		onGrieferGames = true;
	}

	@EventListener
	public void onCitybuildJoin(CitybuildJoinEvent event) {
		onCitybuild = true;
	}

	@EventListener
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		onCitybuild = false;
	}

	@EventListener(priority = Priority.LOWEST)
	public void onServerQuit(ServerEvent.ServerQuitEvent event) {
		onGrieferGames = false;
	}

}
