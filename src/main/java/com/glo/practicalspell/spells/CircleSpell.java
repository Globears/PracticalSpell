package com.glo.practicalspell.spells;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class CircleSpell implements Spell {

    @Override
    public String name() {
        return "circle";
    }

    @Override
    public void castServer(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.scale(5.0));
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
    }
}
