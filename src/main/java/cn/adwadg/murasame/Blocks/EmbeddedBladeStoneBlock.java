package cn.adwadg.murasame.Blocks;

import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import cn.adwadg.murasame.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class EmbeddedBladeStoneBlock extends Block implements EntityBlock {
    // 定义亡灵生物类型
    private static final EntityType<?>[] UNDEAD_MOBS = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.WITHER_SKELETON,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.DROWNED,
            EntityType.PHANTOM,
            EntityType.WITHER,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.SKELETON_HORSE,
            EntityType.ZOMBIE_HORSE
    };

    public EmbeddedBladeStoneBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EmbeddedBladeStoneEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EmbeddedBladeStoneEntity stone) {
            // BlockItem has already applied copied NBT/components by this point.
            // Always reset, including replacement at the original coordinates.
            stone.resetForPlacement();
            stone.syncTrackingPlayers();
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof EmbeddedBladeStoneEntity stone) stone.syncTrackingPlayers();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof EmbeddedBladeStoneEntity entity)) {
                return InteractionResult.FAIL;
            }
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
            if (entity.hasClaimed(player.getUUID())) {
                entity.syncTo(serverPlayer);
                player.displayClientMessage(Component.translatable("message.murasame.already_claimed"), true);
                return InteractionResult.FAIL;
            }

            // 检查等级要求
            if (player.experienceLevel < ModConfig.GAINLEVELNEEDED.get()) {
                player.displayClientMessage(Component.translatable("message.murasame.need_level"), true);
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 0.8f, 1.0f);
                return InteractionResult.FAIL;
            }

            // 检查击杀亡灵生物数量
            if (!hasKilledEnoughUndead(player)) {
                player.displayClientMessage(Component.translatable("message.murasame.need_more_undead_kills"), true);
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 0.8f, 1.0f);
                return InteractionResult.FAIL;
            }

            // 检查正面buff数量
            if (!hasEnoughPositiveBuffs(player)) {
                player.displayClientMessage(Component.translatable("message.murasame.need_more_buffs"), true);
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 0.8f, 1.0f);
                return InteractionResult.FAIL;
            }

            // 所有条件满足，给予拔刀剑
            ItemStack blade = entity.getBladeStack();
            if (blade.isEmpty() || !entity.tryClaim(serverPlayer)) return InteractionResult.FAIL;
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
            level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.8f, 1.0f);

            if (!player.getInventory().add(blade)) {
                player.drop(blade, false);
            }


            // 授予成就 - 现在会验证拔刀剑属性
            cn.adwadg.murasame.advancement.ModAdvancements.grantFromStone(serverPlayer);

            if(ModConfig.GAINCLEANLEVEL.get()){
                player.giveExperienceLevels(-ModConfig.GAINLEVELNEEDED.get());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // 检查是否击杀足够多的亡灵生物
    private boolean hasKilledEnoughUndead(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            int totalUndeadKills = 0;
            for (EntityType<?> undead : UNDEAD_MOBS) {
                totalUndeadKills += serverPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(undead));
            }
            return totalUndeadKills >= ModConfig.GAINKILLNEEDED.get();
        }
        return false;
    }

    // 检查是否有足够多的正面buff
    private boolean hasEnoughPositiveBuffs(Player player) {
        List<MobEffectInstance> positiveEffects = player.getActiveEffects()
                .stream()
                .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL)
                .collect(Collectors.toList());
        return positiveEffects.size() >= ModConfig.GAINEFFECTSNEEDED.get();
    }
}
