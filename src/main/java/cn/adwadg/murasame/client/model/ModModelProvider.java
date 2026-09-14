package cn.adwadg.murasame.client.model;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModModelProvider {
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.Q_PLAYER, QPlayerModel::createBodyLayer);
    }
}
