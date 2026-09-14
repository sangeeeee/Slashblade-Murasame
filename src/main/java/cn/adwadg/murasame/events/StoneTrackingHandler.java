package cn.adwadg.murasame.events;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

@EventBusSubscriber(modid = Murasame.MOD_ID)
public final class StoneTrackingHandler {
    @SubscribeEvent
    public static void chunkSent(ChunkWatchEvent.Sent event) {
        // Sent runs after the client receives the chunk and its block entities.
        for (var entity : event.getChunk().getBlockEntities().values()) {
            if (entity instanceof EmbeddedBladeStoneEntity stone) stone.syncTo(event.getPlayer());
        }
    }
}
