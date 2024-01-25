/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

@Singleton
public class AntiCineScope extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Schwarze Balken unterdrücken")
		.description("Unterdrückt das Erzwingen der schwarzen Balken auf der oberen und unteren Seite des Bildschirms durch GrieferGames.")
		.icon("crossed_out_camera");

	@EventListener
	private void onPacketReceive(PacketReceiveEvent<S3FPacketCustomPayload> event) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		if (!event.packet.getChannelName().equals("labymod3:main") && !event.packet.getChannelName().equals("LMC"))
			return;

		if (event.packet.getBufferData().readableBytes() <= 0)
			return;

		if ("cinescopes".equals(event.packet.getBufferData().readStringFromBuffer(Short.MAX_VALUE)))
			event.cancel();
	}

}
