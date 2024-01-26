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

package dev.l3g7.griefer_utils.laby4.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;

public class ButtonSettingImpl extends AbstractSettingImpl<ButtonSetting, Object> implements ButtonSetting {

	private Icon buttonIcon;

	public ButtonSettingImpl() {
		super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	}

	@Override
	protected Widget[] createWidgets() {
		return new Widget[]{
			buttonIcon == null ?
				ButtonWidget.component(Component.text("§cNo icon"), null, () -> set(null)) :
				ButtonWidget.icon(buttonIcon, () -> set(null))
		};
	}

	@Override
	public ButtonSetting buttonIcon(Object icon) {
		buttonIcon = SettingsImpl.buildIcon(icon);
		return this;
	}

}
