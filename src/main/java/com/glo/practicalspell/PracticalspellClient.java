package com.glo.practicalspell;

import com.glo.practicalspell.client.ClientSpellManager;
import com.glo.practicalspell.client.MagicBoltRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Practicalspell.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Practicalspell.MODID, value = Dist.CLIENT)
public class PracticalspellClient {

    private static KeyMapping drawKey;

    public PracticalspellClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerRenderers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Practicalspell.MAGIC_BOLT.get(), MagicBoltRenderer::new);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        drawKey = new KeyMapping(
                "key.practicalspell.draw",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_TAB,
                "key.categories.practicalspell"
        );
        event.register(drawKey);

        new ClientSpellManager(drawKey);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Practicalspell.LOGGER.info("HELLO FROM CLIENT SETUP");
        Practicalspell.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
