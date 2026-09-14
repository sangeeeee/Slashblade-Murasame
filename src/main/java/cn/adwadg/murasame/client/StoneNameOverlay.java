package cn.adwadg.murasame.client;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.Registry.ModBlocks;
import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** A contextual name for the local player, without requiring a tooltip mod. */
@EventBusSubscriber(modid = Murasame.MOD_ID, value = Dist.CLIENT)
public final class StoneNameOverlay {
    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var client = Minecraft.getInstance();
        if (client.level == null || client.options.hideGui || client.screen != null
                || !(client.hitResult instanceof BlockHitResult hit)) return;
        Component name;
        if (client.level.getBlockEntity(hit.getBlockPos()) instanceof EmbeddedBladeStoneEntity stone) {
            if (!stone.isClientViewKnown()) return;
            name = stone.getClientDisplayName();
        } else if (client.level.getBlockState(hit.getBlockPos()).is(ModBlocks.EMPTY_STONE.get())) {
            name = Component.translatable("block.murasame.empty_stone");
        } else return;
        var gui = event.getGuiGraphics();
        int width = client.font.width(name);
        int x = (gui.guiWidth() - width) / 2;
        int y = gui.guiHeight() / 2 + 16;
        gui.fill(x - 4, y - 3, x + width + 4, y + client.font.lineHeight + 3, 0x90000000);
        gui.drawString(client.font, name, x, y, 0xFFFFFFFF);
    }
}
