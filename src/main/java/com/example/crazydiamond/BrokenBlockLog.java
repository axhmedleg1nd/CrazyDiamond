package com.example.crazydiamond;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Remembers blocks smashed by the stand so "Return Block" can put them back.
 * Blocks are broken WITHOUT drops, so restoring them can't duplicate items.
 * The log lives in memory only (it is cleared when the server stops).
 */
public final class BrokenBlockLog {
	private static final int MAX_ENTRIES = 8192;
	private static final Map<Identifier, LinkedHashMap<BlockPos, BlockState>> LOG = new HashMap<>();

	private BrokenBlockLog() {
	}

	public static void record(ServerWorld world, BlockPos pos, BlockState state) {
		LinkedHashMap<BlockPos, BlockState> map =
				LOG.computeIfAbsent(world.getRegistryKey().getValue(), k -> new LinkedHashMap<>());
		map.put(pos.toImmutable(), state);
		if (map.size() > MAX_ENTRIES) {
			Iterator<BlockPos> it = map.keySet().iterator();
			it.next();
			it.remove();
		}
	}

	/** Restores up to {@code max} logged blocks within {@code radius} of {@code center}, nearest first. */
	public static int restoreSome(ServerWorld world, BlockPos center, int radius, int max) {
		Map<BlockPos, BlockState> map = LOG.get(world.getRegistryKey().getValue());
		if (map == null || map.isEmpty()) {
			return 0;
		}

		double radiusSq = (double) radius * radius;
		List<BlockPos> near = new ArrayList<>();
		for (BlockPos pos : map.keySet()) {
			if (pos.getSquaredDistance(center) <= radiusSq) {
				near.add(pos);
			}
		}
		near.sort(Comparator.comparingDouble(p -> p.getSquaredDistance(center)));

		int restored = 0;
		boolean soundPlayed = false;
		for (BlockPos pos : near) {
			if (restored >= max) {
				break;
			}
			if (!world.isChunkLoaded(pos)) {
				continue;
			}
			BlockState state = map.get(pos);
			BlockState current = world.getBlockState(pos);

			if (current.isAir() || current.isReplaceable()) {
				world.setBlockState(pos, state);
				StandActions.gold(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.35);
				if (!soundPlayed) {
					world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 0.8f, 1.0f);
					soundPlayed = true;
				}
				restored++;
			}
			// Either restored, or the spot was taken by something else: forget the entry.
			map.remove(pos);
		}
		return restored;
	}

	public static void clear() {
		LOG.clear();
	}
}
