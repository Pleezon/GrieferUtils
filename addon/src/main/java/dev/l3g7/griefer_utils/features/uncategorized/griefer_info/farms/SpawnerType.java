/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms;

import net.minecraft.util.StatCollector;

import java.util.HashMap;
import java.util.Map;

public class SpawnerType {

	public static Map<String, SpawnerType> SPAWNER_TYPES = new HashMap<>();
	public static Map<String, SpawnerType> TYPES_BY_ID = new HashMap<>();

	static {
		SPAWNER_TYPES.put("Bruchstein", new SpawnerType("Bruchstein", "tile.stonebrick", "cobblestone") {
			public boolean isCobblestone() {
				return true;
			}

			{ setId("qj202x5w"); } // Cobblestone's id is missing from the metadata response :/
		});
		add("Creeper", "Creeper", "creeper");
		add("Dorfbewohner", "Villager", "villager");
		add("Eisengolem", "VillagerGolem", "iron_golem");
		add("Enderdrache", "EnderDragon", "ender_dragon");
		add("Enderman", "Enderman", "enderman");
		add("Endermilbe", "Endermite", "endermite");
		add("Fledermaus", "Bat", "bat");
		add("Ghast", "Ghast", "ghast");
		add("Hexe", "Witch", "witch");
		add("Höhlenspinne", "CaveSpider", "cave_spider");
		add("Huhn", "Chicken", "chicken");
		add("Kaninchen", "Rabbit", "rabbit");
		add("Kuh", "Cow", "cow");
		add("Lohe", "Blaze", "blaze");
		add("Magmaschleim", "LavaSlime", "magma_cube");
		add("Ozelot", "Ozelot", "ocelot");
		add("Pferd", "EntityHorse", "horse");
		add("Pilzkuh", "MushroomCow", "mooshroom");
		add("Riese", "Giant", "zombie");
		add("Schaf", "Sheep", "sheep");
		add("Schleim", "Slime", "slime");
		add("Schneegolem", "SnowMan", "snow_golem");
		add("Schwein", "Pig", "pig");
		add("Silberfischchen", "Silverfish", "silverfish");
		add("Skelett", "Skeleton", "skeleton");
		add("Spinne", "Spider", "spider");
		add("Tintenfisch", "Squid", "squid");
		add("Wächter", "Guardian", "guardian");
		add("Wither", "WitherBoss", "wither");
		add("Wolf", "Wolf", "wolf");
		add("Zombie", "Zombie", "zombie");
		add("Zombie Pigman", "PigZombie", "pig_zombie");
	}

	private static void add(String name, String translationKey, String texture) {
		SPAWNER_TYPES.put(name, new SpawnerType(name, "entity." + translationKey, "griefer_info/mob_icons/" + texture));
	}

	public final String germanName;
	public final String[] otherNames;
	public final String texture;
	public String id;

	private SpawnerType(String germanName, String translationKey, String texture) {
		this.germanName = germanName;
		if (translationKey == null) {
			otherNames = new String[0];
		} else {
			otherNames = new String[] {
				StatCollector.translateToLocal(translationKey + ".name").toLowerCase(),
				StatCollector.translateToFallback(translationKey + ".name").toLowerCase()
			};
		}

		this.texture = texture;
	}

	public boolean isCobblestone() {
		return false;
	}

	public boolean matchesFilter(String filter) {
		filter = filter.toLowerCase();

		for (String otherName : otherNames)
			if (otherName.contains(filter))
				return true;

		return germanName.toLowerCase().contains(filter);
	}

	public void setId(String id) {
		this.id = id;
		TYPES_BY_ID.put(id, this);
	}


}
