package cn.adwadg.murasame.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NetworkHandler {
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("3");
        registrar.playToServer(ToggleKeyPacket.TYPE, ToggleKeyPacket.STREAM_CODEC, ToggleKeyPacket::handle);
        registrar.playToClient(StoneStatePacket.TYPE, StoneStatePacket.STREAM_CODEC, StoneStatePacket::handle);
    }
}
