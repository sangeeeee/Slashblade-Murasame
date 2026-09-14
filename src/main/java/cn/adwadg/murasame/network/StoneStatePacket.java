package cn.adwadg.murasame.network;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

/** Contains only the recipient's view, never the stone's list of claimants. */
public record StoneStatePacket(ResourceLocation dimension, BlockPos pos, UUID stoneId, boolean claimed)
        implements CustomPacketPayload {
    public static final Type<StoneStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, "stone_state"));
    public static final StreamCodec<ByteBuf, StoneStatePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, StoneStatePacket::dimension,
            BlockPos.STREAM_CODEC, StoneStatePacket::pos,
            UUIDUtil.STREAM_CODEC, StoneStatePacket::stoneId,
            ByteBufCodecs.BOOL, StoneStatePacket::claimed, StoneStatePacket::new);

    @Override
    public Type<StoneStatePacket> type() { return TYPE; }

    public static void handle(StoneStatePacket packet, IPayloadContext context) {
        var level = context.player().level();
        if (level.isClientSide() && level.dimension().location().equals(packet.dimension())
                && level.getBlockEntity(packet.pos()) instanceof EmbeddedBladeStoneEntity stone) {
            stone.applyClientView(packet.stoneId(), packet.claimed());
        }
    }
}
