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

package dev.l3g7.griefer_utils.v1_8_9.features.modules.money;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;

import java.math.BigDecimal;

@Singleton
public class Earned extends Module {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Verdient")
		.description("Zeigt dir, wie viel Geld du seit deinem Minecraft-Start verdient hast.")
		.icon("coin_pile")
		.subSettings(ButtonSetting.create()
			.name("Zurücksetzen")
			.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
			.icon("arrow_circle")
			.buttonIcon("labymod_3/trash")
			.callback(() -> {
				Received.moneyReceived = BigDecimal.ZERO;
				Spent.moneySpent = BigDecimal.ZERO;
			}));

	@Override
	public String[] getValues() {
		ButtonSetting.create()
			.name("Zurücksetzen")
			.description("Setzt das eingenommene und das ausgegebene Geld zurück.")
			.icon("arrow_circle")
			.buttonIcon("labymod_3/trash");

		return new String[]{Constants.DECIMAL_FORMAT_98.format(Received.moneyReceived.subtract(Spent.moneySpent)) + "$"};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"0$"};
	}

}