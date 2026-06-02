package com.glo.practicalspell.spells;

import com.glo.practicalspell.server.SpellTargets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class StarSpell implements Spell {

    @Override
    public String name() {
        return "star";
    }

    @Override
    public void castServer(ServerPlayer player) {
        Set<LivingEntity> targets = SpellTargets.get(player);
        if (targets.isEmpty()) return;

        for (LivingEntity target : targets) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.serverLevel());
            if (bolt != null) {
                bolt.moveTo(target.getX(), target.getY(), target.getZ());
                player.serverLevel().addFreshEntity(bolt);
            }
        }

        SpellTargets.clear(player);
    }
}
