/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.settings.elements.SliderElement;

/**
 * A setting holding an integer, displayed as a slider.
 */
public class SliderSetting extends SliderElement implements ElementBuilder<SliderSetting>, ValueHolder<SliderSetting, Integer> {

	private final IconStorage iconStorage = new IconStorage();
	private final Storage<Integer> storage = new Storage<>(JsonPrimitive::new, JsonElement::getAsInt, 0);

	public SliderSetting() {
		super("§cNo name set", null, 0);
		addCallback(this::set);
	}

	@Override
	public Storage<Integer> getStorage() {
		return storage;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	/**
	 * Sets the lower limit the value can have.
	 */
	public SliderSetting min(int min) {
		return (SliderSetting) setMinValue(min);
	}

	/**
	 * Sets the upper limit the value can have.
	 */
	public SliderSetting max(int max) {
		return (SliderSetting) setMaxValue(max);
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}
