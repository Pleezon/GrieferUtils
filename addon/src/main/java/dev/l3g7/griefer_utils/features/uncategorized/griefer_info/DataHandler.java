package dev.l3g7.griefer_utils.features.uncategorized.griefer_info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.botshops.BotShop;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.botshops.GuiBotShops;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms.Farm;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms.GuiFarms;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms.SpawnerType;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff.FreeStuff;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff.GuiFreestuff;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff.ItemFilter;
import dev.l3g7.griefer_utils.features.uncategorized.settings.BugReporter;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class DataHandler {

	public static void requestMetaData() {
		new Thread(() -> {
			IOUtil.read("https://griefer.info/grieferutils/farm-meta").asJsonObject(response -> {
				for (JsonElement entry : response.getAsJsonArray("entity")) {
					JsonObject entity = entry.getAsJsonObject();
					String id = entity.get("id").getAsString();
					String name = entity.get("name").getAsString();
					SpawnerType type = SpawnerType.SPAWNER_TYPES.get(name);
					if (type != null)
						type.setId(id);
				}

				requestFarms();
			});

			IOUtil.read("https://grieferutils.l3g7.dev/v2/griefer_info_items").asJsonObject(itemResponse -> {
				onItemResponse(itemResponse);

				IOUtil.read("https://griefer.info/grieferutils/freestuff-meta").asJsonArray(metaResponse -> {
					List<String> missingItems = new ArrayList<>();

					for (JsonElement jsonElement : metaResponse) {
						JsonObject item = jsonElement.getAsJsonObject();
						String id = item.get("id").getAsString();
						String name = item.get("name").getAsString();
						ItemFilter filter = ItemFilter.FILTER.get(name);
						if (filter != null)
							filter.setId(id);
						else
							missingItems.add(name + "," + id);
					}

					if (!missingItems.isEmpty())
						BugReporter.reportError(new Throwable("Missing FSM-Filter: " + String.join(";", missingItems)));

					requestFreestuff();
				});
			});

			requestBotshops();
		}).start();
	}

	private static void onItemResponse(JsonObject response) {
		for (Map.Entry<String, JsonElement> entry : response.entrySet()) {
			JsonObject data = entry.getValue().getAsJsonObject();
			ItemStack stack = ItemUtil.fromNBT(data.get("stack").getAsString());
			ItemFilter itemFilter = new ItemFilter(entry.getKey(), stack, data.has("custom_name"));
			ItemFilter.FILTER.put(entry.getKey(), itemFilter);

			int compactCategories = data.get("categories").getAsInt();
			for (int i = 0; i < ItemFilter.CATEGORIES.size(); i++)
				if ((compactCategories & 1 << i) != 0)
					ItemFilter.CATEGORIES.get(i).itemFilters.add(itemFilter);
		}
	}

	public static void requestFarms() {
		Farm.FARMS.clear();
		IOUtil.read("https://griefer.info/grieferutils/farm?cb=0&passive=0&aktive=0&entity=0&order=0").asJsonArray(response -> {
			for (JsonElement entry : response)
				Farm.FARMS.add(Farm.fromJson(entry.getAsJsonObject()));

			if (mc().currentScreen instanceof GuiFarms)
				((GuiFarms) mc().currentScreen).onEntryData();
		});
	}

	public static void requestFreestuff() {
		FreeStuff.FREE_STUFF.clear();
		IOUtil.read("https://griefer.info/grieferutils/freestuff?cb=0&item=0").asJsonArray(response -> {
			for (JsonElement entry : response)
				FreeStuff.FREE_STUFF.add(FreeStuff.fromJson(entry.getAsJsonObject()));

			if (mc().currentScreen instanceof GuiFreestuff)
				((GuiFreestuff) mc().currentScreen).onEntryData();
		});
	}

	public static void requestBotshops() {
		BotShop.BOT_SHOPS.clear();
		IOUtil.read("https://griefer.info/grieferutils/botshops?cb=0&ankauf=0&verkauf=0").asJsonArray(response -> {
			for (JsonElement entry : response)
				BotShop.BOT_SHOPS.add(BotShop.fromJson(entry.getAsJsonObject()));

			if (mc().currentScreen instanceof GuiBotShops)
				((GuiBotShops) mc().currentScreen).onEntryData();
		});
	}

}
