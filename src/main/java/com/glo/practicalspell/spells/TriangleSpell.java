package com.glo.practicalspell.spells;

import com.glo.practicalspell.Practicalspell;
import com.glo.practicalspell.entity.MagicBolt;
import net.minecraft.server.level.ServerPlayer;

public class TriangleSpell implements Spell {

    @Override
    public String name() {
        return "triangle";
    }

    @Override
    public void castServer(ServerPlayer player) {
        MagicBolt bolt = new MagicBolt(
                player.serverLevel(), player,
                Practicalspell.MAGIC_BOLT.get()
        );
        bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 4.0f, 0);
        player.serverLevel().addFreshEntity(bolt);
    }
}
