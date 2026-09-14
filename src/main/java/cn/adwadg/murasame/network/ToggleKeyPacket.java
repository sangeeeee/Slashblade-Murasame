package cn.adwadg.murasame.network;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.events.PlayerSoulHandler;
import cn.adwadg.murasame.playerdata.PlayerSoulDataManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleKeyPacket() implements CustomPacketPayload {
    public static final Type<ToggleKeyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, "toggle_soul"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleKeyPacket> STREAM_CODEC = StreamCodec.unit(new ToggleKeyPacket());
    @Override
    public Type<ToggleKeyPacket> type() { return TYPE; }
    public static void handle(ToggleKeyPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.isAlive()) {
            var data = PlayerSoulDataManager.getOrCreatePlayerData(player);
            data.setShowMurasameSoul(!data.shouldShowMurasameSoul());
            PlayerSoulHandler.updateSoulState(player);
        }
    }
}
