/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.api.misc;

import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.util.StringUtil;

import static dev.l3g7.griefer_utils.api.bridges.MinecraftBridge.minecraftBridge;

public enum Citybuild implements Named {

	ANY("Egal","Egal"),

	CB1,
	CB2,
	CB3,
	CB4,
	CB5,
	CB6,
	CB7,
	CB8,
	CB9,
	CB10,
	CB11,
	CB12,
	CB13,
	CB14,
	CB15,
	CB16,
	CB17,
	CB18,
	CB19,
	CB20,
	CB21,
	CB22,

	NATURE("nature", "Nature", "n"),
	EXTREME("extreme", "Extreme", "x"),
	CBE("cbevil", "Evil", "e", "cbe", "CB Evil"),

	WATER("farm1", "Wasser", "w"),
	LAVA("nether1", "Lava", "l"),
	EVENT("eventserver", "Event", "v");

	private final String internalName;
	private final String displayName;
	private final String[] aliases;

	Citybuild() {
		String id = this.name().substring(2);
		this.internalName = "cb" + id;
		this.displayName = "Citybuild " + id;
		this.aliases = new String[0];
	}

	Citybuild(String internalName, String displayName, String... aliases) {
		this.internalName = internalName;
		this.displayName = displayName;
		this.aliases = aliases;
	}

	public static Citybuild getCitybuild(String cb) {
		cb = cb.toLowerCase();
		if (cb.startsWith("cb"))
			cb = cb.substring(2).trim();

		if (cb.startsWith("citybuild"))
			cb = cb.substring("citybuild".length()).trim();

		if (StringUtil.isNumeric(cb)) {
			try {
				return valueOf("CB" + cb);
			} catch (IllegalArgumentException ignored) {
				return Citybuild.ANY;
			}
		}

		for (Citybuild citybuild : values()) {
			if (citybuild.matches(cb))
				return citybuild;
		}

		return Citybuild.ANY;
	}

	public String getInternalName() {
		return this.internalName;
	}

	public boolean isOnCb() {
		if (this == ANY)
			return true;

		return matches(minecraftBridge.getGrieferGamesSubServer());
	}

	public void join() {
		if (internalName == null)
			throw new IllegalStateException("This citybuild does not exist");

		if (!minecraftBridge.onGrieferGames()) {
			LabyBridge.display(Constants.ADDON_PREFIX + "§fBitte betrete GrieferGames.");
			return;
		}

		String cb = minecraftBridge.getGrieferGamesSubServer();
		if (cb.equals("Portal") || cb.equals("Lobby")) {
			LabyBridge.display(Constants.ADDON_PREFIX + "§fBitte betrete einen Citybuild.");
			return;
		}

		minecraftBridge.send("/switch " + internalName);
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean matches(String cb) {
		if (cb == null)
			return false;

		for (String alias : aliases)
			if (alias.equalsIgnoreCase(cb))
				return true;

		return cb.equalsIgnoreCase(displayName) || cb.equalsIgnoreCase(internalName) || name().equalsIgnoreCase(cb);
	}

	@Override
	public String getName() {
		return displayName;
	}

}
