package com.glo.practicalspell.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.glo.practicalspell.Practicalspell;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = Practicalspell.MODID, value = Dist.CLIENT)
public class KeyMappings {

    public static KeyMapping conjureKey;

    @SubscribeEvent
    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        conjureKey = new KeyMapping(
                "key.practicalspell.conjure",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_TAB,
                "key.categories.practicalspell"
        );
        event.register(conjureKey);

    }
}
