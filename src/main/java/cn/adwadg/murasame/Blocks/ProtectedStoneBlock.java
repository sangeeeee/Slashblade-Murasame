package cn.adwadg.murasame.Blocks;

import cn.adwadg.murasame.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/** Reads the synced server setting at use time, so existing stones follow configuration changes. */
public class ProtectedStoneBlock extends Block {
    public ProtectedStoneBlock(Properties properties) { super(properties); }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return ModConfig.STONE_UNBREAKABLE.get() ? 0 : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return ModConfig.STONE_UNBREAKABLE.get() ? 3600000.0F : super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !ModConfig.STONE_UNBREAKABLE.get() && super.canEntityDestroy(state, level, pos, entity);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return ModConfig.STONE_UNBREAKABLE.get() ? PushReaction.BLOCK : super.getPistonPushReaction(state);
    }
}
