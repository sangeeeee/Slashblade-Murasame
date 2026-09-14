package cn.adwadg.murasame.events;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.Blocks.ProtectedStoneBlock;
import cn.adwadg.murasame.config.ModConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Murasame.MOD_ID)
public final class StoneProtectionHandler {
    @SubscribeEvent
    public static void preventBreaking(BlockEvent.BreakEvent event) {
        // Like bedrock, protection blocks survival mining but permits creative removal.
        if (ModConfig.STONE_UNBREAKABLE.get() && !event.getPlayer().isCreative() && event.getState().getBlock() instanceof ProtectedStoneBlock) {
            event.setCanceled(true);
        }
    }
}
