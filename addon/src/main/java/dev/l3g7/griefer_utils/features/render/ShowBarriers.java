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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.render.RenderBarrierCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import net.labymod.utils.Material;

/**
 * Shows barriers.
 */
@Singleton
public class ShowBarriers extends Feature {

	private final KeySetting key = new KeySetting()
		.name("Taste")
		.icon("key")
		.description("Die Taste, mit der das Anzeigen von Barrien an-/ausgeschalten wird.")
		.pressCallback(pressed -> {
			if (pressed) {
				BooleanSetting enabled = ((BooleanSetting) getMainElement());
				enabled.set(!enabled.get());
			}
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Barrieren anzeigen")
		.description("Fügt Partikel bei Barrieren-Blöcken hinzu.")
		.icon(Material.BARRIER)
		.subSettings(key);

	@EventListener
	public void onDisplayNameRender(RenderBarrierCheckEvent event) {
		event.renderBarrier = true;
	}

}
