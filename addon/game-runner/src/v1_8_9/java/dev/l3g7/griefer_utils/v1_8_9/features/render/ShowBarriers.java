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

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderBarrierCheckEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import net.minecraft.init.Blocks;

/**
 * Shows barriers.
 */
@Singleton
public class ShowBarriers extends Feature {

	private final KeySetting key = KeySetting.create()
		.name("Taste")
		.icon("key")
		.description("Die Taste, mit der das Anzeigen von Barrien an-/ausgeschalten wird.")
		.pressCallback(pressed -> {
			if (pressed) {
				SwitchSetting enabled = ((SwitchSetting) getMainElement());
				enabled.set(!enabled.get());
			}
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Barrieren anzeigen")
		.description("Fügt Partikel bei Barrieren-Blöcken hinzu.")
		.icon(Blocks.barrier)
		.subSettings(key);

	@EventListener
	public void onDisplayNameRender(RenderBarrierCheckEvent event) {
		event.renderBarrier = true;
	}

}
