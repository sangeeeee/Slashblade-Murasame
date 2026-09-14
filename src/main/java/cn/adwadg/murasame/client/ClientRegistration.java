package cn.adwadg.murasame.client;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.Registry.KeyBindings;
import cn.adwadg.murasame.Registry.ModEntities;
import cn.adwadg.murasame.client.model.ModModelProvider;
import cn.adwadg.murasame.client.renderer.EmbeddedBladeStoneRenderer;
import cn.adwadg.murasame.client.renderer.MurasameSoulRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Murasame.MOD_ID, value = Dist.CLIENT)
public final class ClientRegistration {
    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent event) {
        KeyBindings.init();
        event.register(KeyBindings.TOGGLE_KEY);
    }
    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModModelProvider.registerLayers(event);
    }
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MURASAME_SOUL.get(), MurasameSoulRenderer::new);
        event.registerBlockEntityRenderer(ModEntities.EMBEDDED_STONE_ENTITY.get(), EmbeddedBladeStoneRenderer::new);
    }
}
