/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_info;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.player.scoreboard.BankScoreboard;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;

@Singleton
public class BankBalance extends SimpleWidget {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Bankguthaben")
		.description("Zeigt das Bankguthaben an.")
		.icon("bank");

	@Override
	public String getValue() {
		if (BankScoreboard.getBankBalance() == -1)
			return "?";

		return DECIMAL_FORMAT_98.format(BankScoreboard.getBankBalance()) + "$";
	}

}
