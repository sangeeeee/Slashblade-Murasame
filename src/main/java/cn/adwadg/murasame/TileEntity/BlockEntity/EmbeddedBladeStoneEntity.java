package cn.adwadg.murasame.TileEntity.BlockEntity;

import cn.adwadg.murasame.Registry.ModEntities;
import cn.adwadg.murasame.client.utils.MurasameBladeHelper;
import cn.adwadg.murasame.network.StoneStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EmbeddedBladeStoneEntity extends BlockEntity {
    private UUID stoneId;
    private BlockPos origin;
    private String originDimension = "";
    private final Set<UUID> claimants = new HashSet<>();
    private boolean clientViewKnown;
    private boolean clientClaimed;

    public EmbeddedBladeStoneEntity(BlockPos pos, BlockState state) {
        super(ModEntities.EMBEDDED_STONE_ENTITY.get(), pos, state);
    }

    public ItemStack getBladeStack() {
        return level == null ? ItemStack.EMPTY : MurasameBladeHelper.createMurasameBlade(level.registryAccess());
    }

    public UUID getStoneId() {
        ensureIdentity();
        return stoneId;
    }

    private void ensureIdentity() {
        if (!(level instanceof ServerLevel)) return;
        // Saves retain their identity. NBT copied to another position/dimension does not.
        if (stoneId == null || !worldPosition.equals(origin) || !level.dimension().location().toString().equals(originDimension)) {
            resetForPlacement();
        }
    }

    public void resetForPlacement() {
        if (!(level instanceof ServerLevel)) return;
        stoneId = UUID.randomUUID();
        origin = worldPosition.immutable();
        originDimension = level.dimension().location().toString();
        claimants.clear();
        setChanged();
    }

    public boolean hasClaimed(UUID playerId) {
        ensureIdentity();
        return claimants.contains(playerId);
    }

    public boolean tryClaim(ServerPlayer player) {
        if (!(level instanceof ServerLevel) || player.level() != level || isRemoved()) return false;
        ensureIdentity();
        if (!claimants.add(player.getUUID())) return false;
        setChanged();
        syncTo(player);
        return true;
    }

    public StoneStatePacket viewFor(ServerPlayer player) {
        ensureIdentity();
        return new StoneStatePacket(level.dimension().location(), worldPosition, stoneId, hasClaimed(player.getUUID()));
    }

    public void syncTo(ServerPlayer player) {
        if (level instanceof ServerLevel && player.level() == level && !isRemoved()
                && net.neoforged.neoforge.network.registration.NetworkRegistry.hasChannel(player.connection, StoneStatePacket.TYPE.id()))
            PacketDistributor.sendToPlayer(player, viewFor(player));
    }

    public void syncTrackingPlayers() {
        if (!(level instanceof ServerLevel serverLevel) || isRemoved()) return;
        ensureIdentity();
        // Generic BE updates contain only identity. Personal flags travel in separate packets.
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        for (var player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)) {
            // sendBlockUpdated is batched; send identity first so a replacement's personal packet is not rejected.
            player.connection.send(getUpdatePacket());
            syncTo(player);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            ensureIdentity();
            // Also covers /setblock, structures and blocks placed while a chunk is already watched.
            serverLevel.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ensureIdentity();
        if (stoneId != null) tag.putUUID("StoneId", stoneId);
        if (origin != null) tag.putLong("OriginPos", origin.asLong());
        tag.putString("OriginDimension", originDimension);
        ListTag players = new ListTag();
        claimants.stream().sorted().forEach(id -> players.add(StringTag.valueOf(id.toString())));
        tag.put("ClaimedPlayers", players);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stoneId = tag.hasUUID("StoneId") ? tag.getUUID("StoneId") : null;
        origin = tag.contains("OriginPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("OriginPos")) : null;
        originDimension = tag.getString("OriginDimension");
        claimants.clear();
        for (Tag value : tag.getList("ClaimedPlayers", Tag.TAG_STRING)) {
            try { claimants.add(UUID.fromString(value.getAsString())); }
            catch (IllegalArgumentException ignored) { /* Ignore malformed entries, not the whole save. */ }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        ensureIdentity();
        var tag = new CompoundTag();
        if (stoneId != null) tag.putUUID("StoneId", stoneId);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        UUID incoming = tag.hasUUID("StoneId") ? tag.getUUID("StoneId") : null;
        if (!java.util.Objects.equals(stoneId, incoming)) {
            stoneId = incoming;
            clientViewKnown = false;
            clientClaimed = false;
        }
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }

    public void applyClientView(UUID id, boolean claimed) {
        if (level == null || !level.isClientSide() || (stoneId != null && !stoneId.equals(id))) return;
        stoneId = id;
        clientClaimed = claimed;
        clientViewKnown = true;
    }

    public boolean isClientViewKnown() { return clientViewKnown; }
    public boolean shouldRenderBlade() { return clientViewKnown && !clientClaimed; }
    public Component getClientDisplayName() {
        return Component.translatable(clientClaimed ? "block.murasame.empty_stone" : "block.murasame.embedded_stone");
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        // Ctrl+pick-block must not serialize identity or other players' claim records.
        stack.remove(DataComponents.BLOCK_ENTITY_DATA);
    }
}
