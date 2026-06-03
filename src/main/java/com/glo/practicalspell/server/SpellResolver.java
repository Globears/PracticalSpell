package com.glo.practicalspell.server;

import com.glo.practicalspell.network.CastSpellPayload;
import com.glo.practicalspell.spells.Spell;
import com.glo.practicalspell.spells.SpellFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpellResolver {

    public static void handle(CastSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                        Component.literal("[Server] Spell cast: " + payload.spellName())
                );

                Spell spell = SpellFactory.create(payload.spellName());
                if (spell != null) {
                    spell.castServer(sp);
                }
            }
        });
    }
}
