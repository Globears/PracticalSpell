package com.glo.practicalspell.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class SpellTargets {

    private static final Map<UUID, Set<LivingEntity>> playerTargets = new HashMap<>();

    public static void add(ServerPlayer player, LivingEntity target) {
        playerTargets.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(target);
    }

    public static void remove(ServerPlayer player, LivingEntity target) {
        Set<LivingEntity> set = playerTargets.get(player.getUUID());
        if (set != null) {
            set.remove(target);
            if (set.isEmpty()) playerTargets.remove(player.getUUID());
        }
    }

    public static Set<LivingEntity> get(ServerPlayer player) {
        return playerTargets.getOrDefault(player.getUUID(), Collections.emptySet());
    }

    public static void clear(ServerPlayer player) {
        playerTargets.remove(player.getUUID());
    }
}
