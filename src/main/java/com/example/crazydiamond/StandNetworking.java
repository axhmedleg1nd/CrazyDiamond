package com.example.crazydiamond;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;

/** Key presses arrive here from the client as a single int ("action"). */
public final class StandNetworking {
	public static final Identifier ACTION = new Identifier(CrazyDiamondMod.MOD_ID, "action");

	public static final int SUMMON = 0;
	public static final int CYCLE = 1;
	public static final int USE = 2;

	private static final Map<UUID, Ability> SELECTED = new HashMap<>();

	private StandNetworking() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(ACTION, (server, player, handler, buf, responseSender) -> {
			int action = buf.readVarInt();
			server.execute(() -> handle(player, action));
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			BrokenBlockLog.clear();
			SELECTED.clear();
		});
	}

	private static Ability selected(ServerPlayerEntity player) {
		return SELECTED.getOrDefault(player.getUuid(), Ability.PUNCH);
	}

	private static void handle(ServerPlayerEntity player, int action) {
		switch (action) {
			case SUMMON -> toggleStand(player);
			case CYCLE -> {
				Ability next = selected(player).next();
				SELECTED.put(player.getUuid(), next);
				player.sendMessage(Text.translatable("message.crazydiamond.selected", next.displayName()), true);
			}
			case USE -> useAbility(player);
			default -> {
			}
		}
	}

	public static StandEntity findStand(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		List<? extends StandEntity> list = world.getEntitiesByType(
				TypeFilter.instanceOf(StandEntity.class),
				stand -> player.getUuid().equals(stand.getOwnerUuid()));
		return list.isEmpty() ? null : list.get(0);
	}

	private static void toggleStand(ServerPlayerEntity player) {
		StandEntity existing = findStand(player);
		if (existing != null) {
			existing.discard();
			player.sendMessage(Text.translatable("message.crazydiamond.dismissed"), true);
			return;
		}

		ServerWorld world = player.getServerWorld();
		StandEntity stand = new StandEntity(ModEntities.STAND, world);
		stand.setOwner(player);
		stand.setPosition(player.getX(), player.getY(), player.getZ());
		world.spawnEntity(stand);

		StandActions.gold(world, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.6);
		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.7f, 1.6f);
		player.sendMessage(Text.translatable("message.crazydiamond.summoned"), true);
	}

	private static void useAbility(ServerPlayerEntity player) {
		StandEntity stand = findStand(player);
		if (stand == null) {
			player.sendMessage(Text.translatable("message.crazydiamond.no_stand"), true);
			return;
		}
		Ability ability = selected(player);
		if (stand.tryStart(ability)) {
			player.sendMessage(ability.displayName(), true);
		}
	}
}
