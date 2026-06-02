package com.glo.practicalspell.spells;

import net.minecraft.server.level.ServerPlayer;

/** A spell that can be cast on the server side. */
public interface Spell {
    String name();
    void castServer(ServerPlayer player);
}
