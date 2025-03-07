/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.network;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S38PacketPlayerListItem.AddPlayerData;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.minecraft.network.play.server.S38PacketPlayerListItem.Action.*;

/**
 * An event related to the tab list.
 */
public class TabListEvent extends Event {

	private static final Map<UUID, IChatComponent> cachedNames = new HashMap<>();
	private static final Map<UUID, String> uuidToNameMap = new HashMap<>();
	public final long readTime = getLastReadTime();

	public static void updatePlayerInfoList() {
		if (mc().getNetHandler() == null)
			return;

		for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
			IChatComponent originalComponent = cachedNames.get(info.getGameProfile().getId());

			if (originalComponent == null)
				continue;

			// create full deep-copy of component
			TabListNameUpdateEvent event = new TabListNameUpdateEvent(info.getGameProfile(), originalComponent);
			event.fire();
			info.setDisplayName(event.component);
		}
	}

	public static IChatComponent getCachedName(UUID uuid) {
		return cachedNames.get(uuid);
	}

	@EventListener
	public static void onServerLeave(ServerEvent.ServerQuitEvent event) {
		cachedNames.clear();
	}

	/**
	 * An event being posted when a tab list entry updates its name.
	 */
	public static class TabListNameUpdateEvent extends TabListEvent {

		public final GameProfile profile;
		public IChatComponent component;

		private TabListNameUpdateEvent(GameProfile profile, IChatComponent component) {
			this.profile = profile;
			this.component = component.createCopy();
			deepCopyStyle(this.component);
		}

		private void deepCopyStyle(IChatComponent component) {
			component.setChatStyle(component.getChatStyle().createDeepCopy());
			component.getSiblings().forEach(this::deepCopyStyle);
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent<S38PacketPlayerListItem> event) {

			// Ignore packets not updating name
			if (event.packet.getAction() != ADD_PLAYER && event.packet.getAction() != UPDATE_DISPLAY_NAME)
				return;

			for (AddPlayerData data : event.packet.getEntries()) {
				if (data.getDisplayName() == null)
					continue;

				// Post TabListNameUpdateEvent
				TabListNameUpdateEvent tabListEvent = new TabListNameUpdateEvent(data.getProfile(), data.getDisplayName());
				tabListEvent.fire();

				// Update values
				cachedNames.put(data.getProfile().getId(), data.getDisplayName());
				Reflection.set(data, "displayName", tabListEvent.component);
			}
		}

	}

	/**
	 * An event being posted when a player is added to the tab list.
	 */
	public static class TabListPlayerAddEvent extends TabListEvent {

		public final AddPlayerData data;

		public TabListPlayerAddEvent(AddPlayerData data) {
			this.data = data;
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent<S38PacketPlayerListItem> event) {
			if (event.packet.getAction() != ADD_PLAYER)
				return;

			for (AddPlayerData data : event.packet.getEntries()) {
				if (PlayerUtil.isValid(data.getProfile().getName())) {
					uuidToNameMap.putIfAbsent(data.getProfile().getId(), data.getProfile().getName());
					new TabListPlayerAddEvent(data).fire();
				}
			}

		}

	}

	/**
	 * An event being posted when a player is removed from the tab list.
	 */
	public static class TabListPlayerRemoveEvent extends TabListEvent {

		public final AddPlayerData data;
		public final String cachedName;

		public TabListPlayerRemoveEvent(AddPlayerData data, String cachedName) {
			this.data = data;
			this.cachedName = cachedName;
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent<S38PacketPlayerListItem> event) {
			if (event.packet.getAction() != REMOVE_PLAYER)
				return;

			if (mc().getNetHandler() == null)
				return;

			if (event.packet.getEntries().size() == mc().getNetHandler().getPlayerInfoMap().size())
				// When the whole TabList is affected, a TabListClearEvent is posted instead
				return;

			for (AddPlayerData data : event.packet.getEntries()) {
				String name = uuidToNameMap.get(data.getProfile().getId());
				if (name != null)
					new TabListPlayerRemoveEvent(data, name).fire();
			}
		}

	}

	/**
	 * An event being posted when the tab list is cleared.
	 */
	public static class TabListClearEvent extends TabListEvent {

		public final ImmutableMap<AddPlayerData, String> entries;

		public TabListClearEvent(List<AddPlayerData> entries) {
			Map<AddPlayerData, String> namedEntries = new HashMap<>();

			for (AddPlayerData data : entries) {
				String name = uuidToNameMap.get(data.getProfile().getId());
				if (name != null)
					namedEntries.put(data, name);
			}

			this.entries = ImmutableMap.copyOf(namedEntries);
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent<S38PacketPlayerListItem> event) {
			if (event.packet.getAction() != REMOVE_PLAYER)
				return;

			if (mc().getNetHandler() == null)
				return;

			if (event.packet.getEntries().size() == mc().getNetHandler().getPlayerInfoMap().size())
				new TabListClearEvent(event.packet.getEntries()).fire();
		}

	}

	private static long getLastReadTime() {
		if (mc().getNetHandler() == null)
			return 0;

		Channel channel = Reflection.get(mc().getNetHandler().getNetworkManager(), "channel"); // Getter is only available in Forge
		ChannelHandler timeoutHandler = channel.pipeline().get("timeout");
		if (timeoutHandler == null)
			return 0;

		return Reflection.get(timeoutHandler, "lastReadTime");
	}

}
