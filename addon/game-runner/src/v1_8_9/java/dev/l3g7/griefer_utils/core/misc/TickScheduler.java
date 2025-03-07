/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.core.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent.RenderTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A scheduler for delaying code while staying synchronized with Minecraft's client ticks.
 */
public class TickScheduler {

	private static final Map<Runnable, AtomicInteger> clientTickTasks = new HashMap<>();
	private static final Map<Runnable, AtomicInteger> renderTickTasks = new HashMap<>();

	/**
	 * Runs the given runnable after the given delay in client ticks.
	 */
	public static void runAfterClientTicks(Runnable runnable, int delay) {
		if (delay == 0) {
			runnable.run();
			return;
		}

		synchronized (clientTickTasks) {
			clientTickTasks.put(runnable, new AtomicInteger(delay));
		}
	}

	/**
	 * Runs the given runnable after the given delay in render ticks.
	 */
	public static void runAfterRenderTicks(Runnable runnable, int delay) {
		synchronized (renderTickTasks) {
			renderTickTasks.put(runnable, new AtomicInteger(delay));
		}
	}

	@EventListener
	private static void onClientTick(ClientTickEvent event) {
		synchronized (clientTickTasks) {
			updateTasks(clientTickTasks);
		}
	}

	@EventListener
	private static void onRenderTick(RenderTickEvent event) {
		synchronized (renderTickTasks) {
			updateTasks(renderTickTasks);
		}
	}

	/**
	 * Decreases the ticks after which the task should run and runs it if ticks = 0.
	 */
	private static void updateTasks(Map<Runnable, AtomicInteger> tasks) {
		Iterator<Entry<Runnable, AtomicInteger>> it = new HashMap<>(tasks).entrySet().iterator();
		while (it.hasNext()) {
			// Decrease time, run if 0
			Entry<Runnable, AtomicInteger> entry = it.next();
			if (entry.getValue().decrementAndGet() == 0) {
				it.remove();
				entry.getKey().run();
			}
		}
	}

}
