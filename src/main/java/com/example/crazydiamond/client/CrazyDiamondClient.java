package com.example.crazydiamond.client;

import org.lwjgl.glfw.GLFW;

import com.example.crazydiamond.ModEntities;
import com.example.crazydiamond.StandNetworking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

public class CrazyDiamondClient implements ClientModInitializer {
	private static final String CATEGORY = "category.crazydiamond";

	private static KeyBinding summonKey;
	private static KeyBinding cycleKey;
	private static KeyBinding useKey;

	@Override
	public void onInitializeClient() {
		summonKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.crazydiamond.summon", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));
		cycleKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.crazydiamond.cycle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
		useKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.crazydiamond.use", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY));

		EntityModelLayerRegistry.registerModelLayer(StandModel.LAYER, StandModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.STAND, StandRenderer::new);
		EntityRendererRegistry.register(ModEntities.STONE_SHOT, FlyingItemEntityRenderer::new);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			while (summonKey.wasPressed()) {
				send(StandNetworking.SUMMON);
			}
			while (cycleKey.wasPressed()) {
				send(StandNetworking.CYCLE);
			}
			while (useKey.wasPressed()) {
				send(StandNetworking.USE);
			}
		});
	}

	private static void send(int action) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(action);
		ClientPlayNetworking.send(StandNetworking.ACTION, buf);
	}
}
