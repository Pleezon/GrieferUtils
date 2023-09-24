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

package dev.l3g7.griefer_utils.features.world;

import com.github.lunatrius.schematica.api.ISchematic;
import com.google.common.base.Strings;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.event.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.event.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.event.events.render.ParticleSpawnEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.SchematicaUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static net.labymod.utils.Material.*;
import static net.minecraft.init.Blocks.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * Implements cursor movement, selection and copy and paste in the sign edit gui.
 */
@Singleton
public class RedstoneHelper extends Feature {

	private static final Map<ChunkCoordIntPair, Map<BlockPos, RedstoneRenderObject>> redstoneRenderObjects = new ConcurrentHashMap<>();
	private static final Map<BlockPos, RedstoneRenderObject> schematicasRROs = new ConcurrentHashMap<>();

	private static final int REDSTONE_PARTICLE_ID = EnumParticleTypes.REDSTONE.getParticleID();
	private static Object previousSchematic = null;
	private static BlockPos previousSchematicPos = null;

	private static final BooleanSetting showZeroPower = new BooleanSetting()
		.name("0 anzeigen")
		.icon(REDSTONE);

	private static final BooleanSetting showPower = new BooleanSetting()
		.name("Redstone-Stärke anzeigen")
		.icon(REDSTONE)
		.subSettings(showZeroPower);

	private static final BooleanSetting showDirection = new BooleanSetting()
		.name("Richtung anzeigen")
		.description("Zeigt die Richtung von Werfern / Spendern und Trichtern.")
		.icon(COMPASS);

	private static final BooleanSetting showNoteId = new BooleanSetting()
		.name("Ton-ID anzeigen")
		.description("Ob der Name des Tons oder die ID angezeigt werden soll.")
		.icon(NOTE_BLOCK)
		.defaultValue(true);

	private static final BooleanSetting showNoteBlockPitch = new BooleanSetting()
		.name("Notenblock-Höhe anzeigen")
		.description("Zeigt an, welche Tonhöhe bei Notenblöcken eingestellt ist."
			+ "\nDafür muss von diesem Block ein Ton abgespielt worden sein.")
		.icon(NOTE_BLOCK)
		.subSettings(showNoteId);

	private static final BooleanSetting showCauldronLevel = new BooleanSetting()
		.name("Kessel-Füllstand anzeigen")
		.icon(CAULDRON_ITEM);

	private static final NumberSetting range = new NumberSetting()
		.name("Radius")
		.description("Der Radius um den Spieler in Chunks, in dem die Informationen angezeigt werden."
			+ "\n(-1 ist unendlich)"
			+ "\n(Betrifft nicht Schematics)")
		.defaultValue(-1)
		.min(-1)
		.icon(COMPASS);

	private static final BooleanSetting hideRedstoneParticles = new BooleanSetting()
		.name("Redstone-Partikel verstecken")
		.icon(REDSTONE)
		.defaultValue(true);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Redstone-Helfer")
		.description("Hilft beim Arbeiten mit Redstone.")
		.icon(REDSTONE)
		.subSettings(showPower, showDirection, showNoteBlockPitch, showCauldronLevel, range, new HeaderSetting(), hideRedstoneParticles);

	@EventListener
	public void onChunkFilled(ChunkFilledEvent event) {
		Map<BlockPos, RedstoneRenderObject> renderObjects = new ConcurrentHashMap<>();
		ChunkCoordIntPair coords = new ChunkCoordIntPair(event.getChunk().xPosition, event.getChunk().zPosition);

		for (ExtendedBlockStorage ebs : event.getChunk().getBlockStorageArray()) {
			if (ebs == null)
				continue;

			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						RedstoneRenderObject redstoneRenderObject = RedstoneRenderObject.fromState(ebs.get(x, y, z));
						if (redstoneRenderObject == null)
							continue;

						renderObjects.put(coords.getBlock(x, y + ebs.getYLocation(), z), redstoneRenderObject);
					}
				}
			}
		}

		if (!renderObjects.isEmpty())
			redstoneRenderObjects.put(coords, renderObjects);
	}

	@EventListener
	public void onChunkUnload(ChunkUnloadEvent event) {
		ChunkCoordIntPair coordPair = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		redstoneRenderObjects.remove(coordPair);
	}

	@EventListener
	public void onServerChange(ServerEvent.ServerSwitchEvent event) {
		redstoneRenderObjects.clear();
	}

	@EventListener
	public void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		if (event.packet instanceof S23PacketBlockChange) {
			S23PacketBlockChange packet = (S23PacketBlockChange) event.packet;
			onBlockUpdate(packet.getBlockPosition(), packet.getBlockState());
		}

		if (!(event.packet instanceof S22PacketMultiBlockChange))
			return;

		S22PacketMultiBlockChange packet = (S22PacketMultiBlockChange) event.packet;
		for (S22PacketMultiBlockChange.BlockUpdateData data : packet.getChangedBlocks())
			onBlockUpdate(data.getPos(), data.getBlockState());
	}

	private void onBlockUpdate(BlockPos pos, IBlockState state) {
		RedstoneRenderObject redstoneRenderObject = RedstoneRenderObject.fromState(state);

		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);

		if (redstoneRenderObject != null) {
			Map<BlockPos, RedstoneRenderObject> map = redstoneRenderObjects.computeIfAbsent(pair, k -> new ConcurrentHashMap<>());
			map.put(pos, RedstoneRenderObject.fromState(state));
			return;
		}

		Map<BlockPos, RedstoneRenderObject> map = redstoneRenderObjects.get(pair);
		if (map == null)
			return;

		if (state.getBlock() != noteblock || !showNoteBlockPitch.get())
			map.remove(pos);

		if (map.isEmpty())
			redstoneRenderObjects.remove(pair);
	}

	private void updateSchematic() {
		ISchematic schematic = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getSchematic();
		BlockPos position = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getPosition();

		if (previousSchematic == schematic && Objects.equals(previousSchematicPos, position))
			return;

		schematicasRROs.clear();
		previousSchematic = schematic;
		previousSchematicPos = position == null ? null : position.up().down();

		if (schematic == null || position == null)
			return;

		for (int dX = 0; dX < schematic.getWidth(); dX++) {
			for (int dY = 0; dY < schematic.getHeight(); dY++) {
				for (int dZ = 0; dZ < schematic.getLength(); dZ++) {
					IBlockState state = schematic.getBlockState(new BlockPos(dX, dY, dZ));
					RedstoneRenderObject redstoneRenderObject = RedstoneRenderObject.fromState(state);
					if (redstoneRenderObject == null)
						continue;

					schematicasRROs.put(position.add(dX, dY, dZ), redstoneRenderObject);
				}
			}
		}
	}

	@EventListener
	public void onParticleSpawn(ParticleSpawnEvent event) {
		if (event.particleID == REDSTONE_PARTICLE_ID && hideRedstoneParticles.get())
			event.cancel();
	}

	@EventListener
	private void onNoteBlock(NoteBlockPlayEvent event) {
		BlockPos pos = event.pos;
		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);
		Map<BlockPos, RedstoneRenderObject> map = redstoneRenderObjects.computeIfAbsent(pair, k -> new ConcurrentHashMap<>());
		String name = event.getNote().name().replace("_SHARP", "is");
		name += Strings.repeat("'", event.getOctave().ordinal() + 1);
		String finalName = name;
		map.put(pos, new RedstoneRenderObject.TextRRO(showNoteBlockPitch, () -> showNoteId.get() ? String.valueOf(event.getVanillaNoteId()) : finalName));
	}

	@EventListener
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (!showPower.get() && !showDirection.get() && !showNoteBlockPitch.get() || (redstoneRenderObjects.isEmpty() && schematicasRROs.isEmpty()))
			return;

		if (Constants.SCHEMATICA)
			updateSchematic();

		GlStateManager.disableDepth();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();

		for (Map.Entry<ChunkCoordIntPair, Map<BlockPos, RedstoneRenderObject>> entry : redstoneRenderObjects.entrySet())
			if (range.get() == -1 || (Math.abs(entry.getKey().chunkXPos - player().chunkCoordX) <= range.get()
				&& Math.abs(entry.getKey().chunkZPos - player().chunkCoordZ) <= range.get()))
				for (Map.Entry<BlockPos, RedstoneRenderObject> chunkEntry : entry.getValue().entrySet())
					chunkEntry.getValue().render(chunkEntry.getKey(), event.partialTicks);

		if (Constants.SCHEMATICA)
			renderSchematicasRROs(event.partialTicks);

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();
	}

	private void renderSchematicasRROs(float partialTicks) {
		if (SchematicaUtil.dontRender())
			return;

		for (Map.Entry<BlockPos, RedstoneRenderObject> entry : schematicasRROs.entrySet())
			if (SchematicaUtil.shouldLayerBeRendered(entry.getKey().getY()))
				entry.getValue().render(entry.getKey(), partialTicks);
	}

	private abstract static class RedstoneRenderObject {

		public static RedstoneRenderObject fromState(IBlockState state) {
			Block block = state.getBlock();
			if (block == redstone_wire)
				return new Wire(state.getValue(BlockRedstoneWire.POWER));

			if (block == cauldron)
				return new TextRRO(showCauldronLevel, () -> state.getValue(BlockCauldron.LEVEL));

			boolean isHopper = block == hopper;
			if (!isHopper && block != dropper && block != dispenser)
				return null;

			EnumFacing dir = state.getValue(BlockDispenser.FACING);
			if (isHopper && dir == EnumFacing.DOWN)
				return null;

			return new Directional(dir, isHopper);

		}

		private static void prepareRender(Vec3d loc, float partialTicks) {
			GlStateManager.pushMatrix();
			Entity viewer = mc().getRenderViewEntity();
			double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
			double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
			double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
			double tx = (int) loc.x - viewerX + 0.5;
			double ty = loc.y - viewerY;
			double tz = (int) loc.z - viewerZ + 0.5;
			GlStateManager.translate(tx, ty, tz);

			GlStateManager.enableDepth();
			GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.enableTexture2D();
		}

		public abstract void render(BlockPos pos, float partialTicks);

		private static class Wire extends RedstoneRenderObject {
			private final int power;

			private Wire(int power) {
				this.power = power;
			}

			public void render(BlockPos pos, float partialTicks) {
				if (!showPower.get())
					return;

				if (power <= 0 && !showZeroPower.get())
					return;

				FontRenderer font = mc().fontRendererObj;

				prepareRender(new Vec3d(pos.getX(), pos.getY() + 0.02, pos.getZ()), partialTicks);
				String str = String.valueOf(power);

				GlStateManager.scale(0.035, 0.035, 0.035);
				GlStateManager.rotate(90, 1, 0, 0);
				GlStateManager.rotate(180 + mc().getRenderManager().playerViewY, 0, 0, 1);
				GlStateManager.translate(-(font.getStringWidth(str) - 1) / 2d, -(font.FONT_HEIGHT / 2d - 1), 0);

				font.drawString(str, 0, 0, 0xFFFFFF);

				GlStateManager.popMatrix();
			}
		}

		private static class Directional extends RedstoneRenderObject {
			private final EnumFacing dir;
			private final boolean isHopper;

			private Directional(EnumFacing dir, boolean isHopper) {
				this.dir = dir;
				this.isHopper = isHopper;
			}

			public void render(BlockPos pos, float partialTicks) {
				if (!showDirection.get())
					return;

				prepareRender(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), partialTicks);

				switch (dir) {
					case UP:
						GlStateManager.rotate(270, 0, 0, 1);
						GlStateManager.translate(-0.51, -0.51, 0);
						break;
					case DOWN:
						GlStateManager.rotate(270, 0, 0, -1);
						GlStateManager.translate(0.51, -0.51, 0);
						break;
					case NORTH:
						GlStateManager.rotate(90, 0, -1, 0);
						break;
					case SOUTH:
						GlStateManager.rotate(90, 0, 1, 0);
						break;
					case EAST:
						GlStateManager.rotate(180, 0, 1, 0);
						break;
					case WEST:
						break;
				}
				GlStateManager.translate(-0.35, 0, -0.51);

				GlStateManager.scale(0.1, 0.1, 0.1);

				if (isHopper) {
					GlStateManager.translate(0, 6.9, 0);
					GlStateManager.rotate(90, 1, 0, 0);
					mc().fontRendererObj.drawString("⬅", 0, 0, 0xFFFFFF);
				} else {
					mc().fontRendererObj.drawString("⬅", 0, 0, 0);

					GlStateManager.translate(0, 0, 10.2);

					mc().fontRendererObj.drawString("⬅", 0, 0, 0);
					GlStateManager.translate(0, 10.2, -10.35);

					GlStateManager.rotate(90, 1, 0, 0);
					mc().fontRendererObj.drawString("⬅", 0, 0, 0);

					GlStateManager.translate(0, 0, 10.5);
					mc().fontRendererObj.drawString("⬅", 0, 0, 0);
				}

				GlStateManager.popMatrix();
			}
		}

		private static class TextRRO extends RedstoneRenderObject {

			private final BooleanSetting setting;
			private final Supplier<Object> textSupplier;

			private TextRRO(BooleanSetting setting, Supplier<Object> textSupplier) {
				this.textSupplier = textSupplier;
				this.setting = setting;
			}

			@Override
			public void render(BlockPos pos, float partialTicks) {
				if (!setting.get())
					return;

				prepareRender(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), partialTicks);

				GlStateManager.translate(-0.0175, 0.63, -0.51);
				GlStateManager.scale(-0.035, -0.035, 0.035);

				String text = String.valueOf(textSupplier.get());
				int x = -mc().fontRendererObj.getStringWidth(text) / 2;

				mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

				GlStateManager.translate(-1, 0, 29);
				GlStateManager.scale(-1, 1, 1);

				mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);
				GlStateManager.translate(14, 0, -15);

				GlStateManager.rotate(90, 0, 1, 0);
				mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

				GlStateManager.translate(-1, 0, -29);
				GlStateManager.scale(-1, 1, 1);
				mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

				GlStateManager.scale(-1, 1, 1);
				GlStateManager.translate(4, -11, 15);

				GlStateManager.rotate(90, 1, 0, 0);
				GlStateManager.rotate(90, 0, 0, 1);
				mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

				GlStateManager.translate(0, 7.1, -29.1);
				GlStateManager.scale(1, -1, 1);

				mc().fontRendererObj.drawString(text, x, 0, 0xFFFFFF);

				GlStateManager.popMatrix();
			}

		}

	}

}
