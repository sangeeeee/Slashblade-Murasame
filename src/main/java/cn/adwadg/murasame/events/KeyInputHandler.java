package cn.adwadg.murasame.events;

import cn.adwadg.murasame.Registry.KeyBindings;
import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.network.ToggleKeyPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Murasame.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (KeyBindings.TOGGLE_KEY.consumeClick()) {
            // 发送切换请求到服务端
            PacketDistributor.sendToServer(new ToggleKeyPacket());
        }
    }
}
