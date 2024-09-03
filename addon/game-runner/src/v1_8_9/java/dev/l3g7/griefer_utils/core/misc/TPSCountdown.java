/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S07PacketRespawn;

public class TPSCountdown {

	private long lastServerTime = -1;
	private int serverTicksRemaining; // Ticks remaining
	private int clientTicksRemaining;

	public static TPSCountdown fromMinutes(int minutes) { return fromSeconds(minutes * 60); }
	public static TPSCountdown fromSeconds(int seconds) { return new TPSCountdown(seconds * 20); }
	public static TPSCountdown fromEnd(long end) {
		long ms = end - System.currentTimeMillis();
		return new TPSCountdown((int) (ms / 50));
	}

	public TPSCountdown(int ticksRemaining) {
		serverTicksRemaining = clientTicksRemaining = ticksRemaining;
		EventRegisterer.register(this);
	}

	public int secondsRemaining() {
		return (clientTicksRemaining + 19 /* round up */) / 20;
	}

	public void addMinutes(int minutes) { addTicks(minutes * 1200); }
	public void addTicks(int ticks) {
		serverTicksRemaining += ticks;
		clientTicksRemaining += ticks;
	}

	/**
	 * Returns if the countdown has expired, destroying it if that's the case.
	 */
	public boolean destroyIfExpired() {
		if (clientTicksRemaining > 0)
			return false;

		EventRegisterer.unregister(this);
		return true;
	}

	@EventListener
	private void onTick(ClientTickEvent event) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		clientTicksRemaining--;
	}

	@EventListener
	private void onWorldSwitch(PacketEvent.PacketReceiveEvent<S07PacketRespawn> event) {
		lastServerTime = -1;
	}

	@EventListener
	private void onTimeUpdate(PacketEvent.PacketReceiveEvent<S03PacketTimeUpdate> event) {
		if (lastServerTime == -1) {
			lastServerTime = event.packet.getWorldTime();
			return;
		}

		int passedTicks = (int) (event.packet.getWorldTime() - lastServerTime);
		lastServerTime = event.packet.getWorldTime();

		serverTicksRemaining = Math.max(serverTicksRemaining - passedTicks, clientTicksRemaining);
		clientTicksRemaining = serverTicksRemaining;
	}

}
