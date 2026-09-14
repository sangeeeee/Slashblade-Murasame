package cn.adwadg.murasame.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NetworkHandler {
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToServer(ToggleKeyPacket.TYPE, ToggleKeyPacket.STREAM_CODEC, ToggleKeyPacket::handle);
    }
}
