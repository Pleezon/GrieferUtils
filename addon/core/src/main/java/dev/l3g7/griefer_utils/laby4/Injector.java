/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4;

import dev.l3g7.griefer_utils.injection.InjectorBase;
import net.labymod.api.Laby;
import net.labymod.api.addon.LoadedAddon;
import net.labymod.api.addon.entrypoint.Entrypoint;
import net.labymod.api.addon.transform.AddonClassTransformer;
import net.labymod.api.models.addon.annotation.AddonEntryPoint;
import net.labymod.api.models.addon.annotation.AddonTransformer;
import net.labymod.api.models.version.Version;

@AddonEntryPoint
@AddonTransformer
@SuppressWarnings("UnstableApiUsage")
public class Injector extends InjectorBase implements Entrypoint, AddonClassTransformer {

	@Override
	public void initialize(Version version) {
		LoadedAddon addon = Laby.labyAPI().addonService().getAddon(getClass()).orElseThrow();
		InjectorBase.initialize(addon.info().getNamespace());
	}

}
