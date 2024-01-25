/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.filter_webhooks;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.event.events.render.ChatEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.AddonUtil;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.main.LabyMod;
import net.minecraft.client.gui.GuiScreen;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class FilterWebhooks extends Feature {

    private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final JsonObject EMBED_FOOTER = new JsonObject();

    static final Map<String, String> webhooks = new HashMap<>();

	@MainElement
    private final BooleanSetting enabled = new BooleanSetting()
		.name("Webhooks in Filtern")
		.description("Sendet eine Chatnachricht an einen Discord-Webhook, wenn ein LabyMod-Filter auslöst.")
		.icon("webhook");

    public FilterWebhooks() {
	    EMBED_FOOTER.addProperty("text", Constants.ADDON_NAME + " v" + AddonUtil.getVersion());
	    EMBED_FOOTER.addProperty("icon_url", "https://grieferutils.l3g7.dev/icon/padded/64x64/");
    }

    static void saveWebhooks() {
        JsonObject data = new JsonObject();
        for (Map.Entry<String, String> entry : webhooks.entrySet())
            data.addProperty(entry.getKey(), entry.getValue());
        Config.set("chat.filter_webhooks.filter", data);
        Config.save();
    }

    @OnEnable
    private void loadWebhooks() {
        if (Config.has("chat.filter_webhooks.filter"))
            for (Map.Entry<String, JsonElement> entry : Config.get("chat.filter_webhooks.filter").getAsJsonObject().entrySet()) {
				String value = entry.getValue().getAsString();
				if (!value.trim().isEmpty())
		            webhooks.put(entry.getKey(), entry.getValue().getAsString());
            }
    }

    @EventListener
    public void onGuiOpen(GuiOpenEvent<GuiScreen> event) {
        if (event.gui instanceof GuiChatFilter)
            event.gui = new CustomGuiChatFilter(Reflection.get(event.gui, "defaultInputFieldText"));
    }

    @EventListener(priority = Priority.LOWEST)
    public void onMessageReceive(ChatEvent.ChatMessageAddEvent event) {
        // Check if filters match
        String msg = event.component.getUnformattedText().toLowerCase().replaceAll("§.", "");
        for (Filters.Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
            if (webhooks.containsKey(filter.getFilterName())
	                && !webhooks.get(filter.getFilterName()).trim().isEmpty()
                    && Arrays.stream(filter.getWordsContains()).anyMatch(w -> msg.contains(w.toLowerCase()))
                    && Arrays.stream(filter.getWordsContainsNot()).noneMatch(w -> msg.contains(w.toLowerCase()))) {
                // Build payload
	            JsonObject root = new JsonObject();
				root.add("content", JsonNull.INSTANCE);

	            JsonArray embeds = new JsonArray();
				JsonObject embed = new JsonObject();
				embed.add("title", sanitize(filter.getFilterName()));
				embed.add("description", sanitize(event.component.getUnformattedText().replaceAll("§.", "")));
				embed.add("footer", EMBED_FOOTER);
				if (filter.isHighlightMessage())
					embed.addProperty("color", ((filter.getHighlightColorR() & 0xff) << 16) | ((filter.getHighlightColorG() & 0xff) << 8) | (filter.getHighlightColorB() & 0xff));
	            embeds.add(embed);
				root.add("embeds", embeds);

                // Send to webhook
                EXECUTOR_SERVICE.execute(() -> {
	                try {
		                HttpURLConnection conn = (HttpURLConnection) new URL(webhooks.get(filter.getFilterName()).trim()).openConnection();
		                conn.setConnectTimeout(3000);
		                conn.setReadTimeout(10000);
		                conn.addRequestProperty("User-Agent", "GrieferUtils");
		                conn.addRequestProperty("Content-Type", "application/json");
		                conn.setDoOutput(true);

		                conn.setRequestMethod("POST");

		                try (OutputStream stream = conn.getOutputStream()) {
			                stream.write(root.toString().getBytes(StandardCharsets.UTF_8));
			                stream.flush();
		                }

						conn.getInputStream().close();
	                } catch (Throwable e) {
		                e.printStackTrace();
	                }
                });
            }
        }
    }

	private JsonElement sanitize(String value) {

		if (value == null)
			return JsonNull.INSTANCE;

		value = value.replaceAll("([^a-zA-Z\\d ])", "\\\\$1");
		return new JsonPrimitive(value);

	}

}