/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.player;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.user.util.UserActionEntry;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;

@Singleton
public class CleanUpPlayerMenu extends Feature {

	private List<UserActionEntry> defaultEntries;
	private List<UserActionEntry> allDefaultEntries;
	private String statesKey;
	private int shownEntries;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spielermenü aufräumen")
		.description("Entfernt Spielermenü-Einträge, die eigentlich nicht entfernt werden können.")
		.icon("labymod:chat/playermenu");

	@Override
	public void init() {
		super.init();
		defaultEntries = Reflection.get(labyMod().getUserManager().getUserActionGui(), "defaultEntries");
		allDefaultEntries = new ArrayList<>(defaultEntries);
		shownEntries = 0;

		statesKey = getConfigKey() + ".entries";
		if (Config.has(statesKey)) {
			shownEntries = Config.get(statesKey).getAsInt();
			updateEntries();
		}

		List<BooleanSetting> settings = new ArrayList<>();

		for (int i = 0; i < allDefaultEntries.size(); i++) {
			UserActionEntry entry = allDefaultEntries.get(i);
			int index = 1 << i;

			settings.add(new BooleanSetting()
				.name(entry.getDisplayName())
				.description("Ob der Spielermenü-Eintrag \"" + entry.getDisplayName() + "\" angezeigt werden soll.")
				.icon("labymod:chat/playermenu")
				.defaultValue((shownEntries & index) != 0)
				.callback(b -> {
					if (b)
						shownEntries |= index;
					else
						shownEntries &= ~index;

					updateEntries();
				}));
		}

		enabled.subSettings(settings.toArray(new BooleanSetting[0]));
	}

	private void updateEntries() {
		defaultEntries.clear();

		for (int i = 0; i < allDefaultEntries.size(); i++)
			if ((shownEntries & 1 << i) != 0)
				defaultEntries.add(allDefaultEntries.get(i));

		Config.set(statesKey, new JsonPrimitive(shownEntries));
		Config.save();
	}

}
