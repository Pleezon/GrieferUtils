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

package dev.l3g7.griefer_utils.util;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.labymod.ingamechat.IngameChatManager.INSTANCE;

public class ChatLineUtil {

	public static IChatComponent currentComponent;

	public static final Map<ChatLine, IChatComponent> LINE_TO_COMPONENT = new HashMap<>();
	private static final List<IChatComponent> MODIFIED_COMPONENTS = new ArrayList<>();
	private static final List<IChatComponent> UNMODIFIED_COMPONENTS = new ArrayList<>();

	@EventListener
	private static void onMessageModify(MessageEvent.MessageModifyEvent event) {
		UNMODIFIED_COMPONENTS.add(event.original);
	}

	@EventListener
	private static void onMessageModified(MessageEvent.MessageModifiedEvent event) {
		MODIFIED_COMPONENTS.add(event.component);
	}

	public static IChatComponent getComponentFromLine(ChatLine chatLine) {
		return LINE_TO_COMPONENT.get(chatLine);
	}

	public static IChatComponent getUnmodifiedIChatComponent(IChatComponent iChatComponent) {
		if (iChatComponent == null)
			return null;

		int index = MODIFIED_COMPONENTS.indexOf(iChatComponent);

		if (index == -1)
			return null;

		return UNMODIFIED_COMPONENTS.get(index);
	}

	public static IChatComponent getHoveredComponent() {
		for (ChatRenderer chatRenderer : INSTANCE.getChatRenderers()) {
			IChatComponent component = getHoveringComponent(chatRenderer);
			if (component != null)
				return component;
		}

		return null;
	}

	private static IChatComponent getHoveringComponent(ChatRenderer chatRenderer) {
		if (!chatRenderer.isChatOpen() || !chatRenderer.isMouseOver())
			return null;

		float mouseX = chatRenderer.isRightBound() ? chatRenderer.lastMouseX - chatRenderer.getChatPositionX() + chatRenderer.getChatWidth() + 3 : -(chatRenderer.getChatPositionX() - chatRenderer.lastMouseX);
		float mouseY = -chatRenderer.lastMouseY + chatRenderer.getChatPositionY();
		mouseX /= chatRenderer.getChatScale();
		mouseY /= chatRenderer.getChatScale();
		List<ChatLine> list = new LinkedList<>();

		for (ChatLine chatline : chatRenderer.getChatLines()) {
			if (chatline != null && chatline.getRoom().equals(INSTANCE.getSelectedRoom())) {
				list.add(chatline);
			}
		}

		int hoveredLine = (int)mouseY / Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + chatRenderer.getScrollPos();
		if (hoveredLine < 0 || hoveredLine >= list.size())
			return null;

		ChatLine chatline = list.get(hoveredLine);
		IChatComponent lineComponent = (IChatComponent) chatline.getComponent();
		int x = mc().fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(lineComponent.getFormattedText(), false));
		if (x <= mouseX)
			return null;

		return LINE_TO_COMPONENT.get(chatline);
	}



	@Mixin(value = ChatRenderer.class, remap = false)
	private static class MixinChatRenderer {

		@Redirect(method = "addChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
		public void postChatLineAddEvent(List<Object> instance, int i, Object o) {
			LINE_TO_COMPONENT.put((ChatLine) o, currentComponent);
			instance.add(i, o);
		}

	}

	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
		public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			currentComponent = component;
		}

	}

}
