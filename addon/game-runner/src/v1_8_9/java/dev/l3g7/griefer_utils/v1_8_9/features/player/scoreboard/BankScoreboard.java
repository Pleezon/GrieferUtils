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

package dev.l3g7.griefer_utils.v1_8_9.features.player.scoreboard;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModPayloadEvent;

@Singleton
public class BankScoreboard extends ScoreboardHandler.ScoreboardMod {

	private static long bankBalance = -1;

	public static long getBankBalance() {
		return bankBalance;
	}

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Bankguthaben im Scoreboard")
		.description("Fügt das Bankguthaben im Scoreboard hinzu.")
		.icon("bank");

	public BankScoreboard() {
		super("Bankguthaben", 1);
	}

	@EventListener(triggerWhenDisabled = true, priority = Priority.HIGH)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (event.channel.equals("bank"))
			bankBalance = event.payload.getAsJsonObject().get("amount").getAsLong();
	}

	@Override
	protected String getValue() {
		return bankBalance == -1 ? "?" : Constants.DECIMAL_FORMAT_98.format(bankBalance) + "$";
	}

}