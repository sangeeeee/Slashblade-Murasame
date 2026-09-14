package cn.adwadg.murasame.events;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.Registry.ModEntities;
import cn.adwadg.murasame.Entities.EntityMurasameSoul;
import cn.adwadg.murasame.playerdata.PlayerSoulDataManager;
import cn.adwadg.murasame.playerdata.PlayerSoulData;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;

@EventBusSubscriber(modid = Murasame.MOD_ID)
public class PlayerSoulHandler {
    private static final Map<UUID, EntityMurasameSoul> soulMap = new HashMap<>();

    private static void spawnSoulEntity(Player player) {
        try {
            // 检查已有实体
            EntityMurasameSoul existing = soulMap.get(player.getUUID());
            if (existing != null && existing.isAlive() && existing.level() == player.level()) {
                return; // 如果已有实体，不再创建新实体
            }

            PlayerSoulData playerData = PlayerSoulDataManager.getOrCreatePlayerData(player);
            // 如果玩家设置为隐藏，则不创建实体
            if (!playerData.shouldShowMurasameSoul()) {
                return;
            }

            removeSoul(player);
            ServerLevel level = (ServerLevel) player.level();
            EntityMurasameSoul soul = new EntityMurasameSoul(ModEntities.MURASAME_SOUL.get(), level);

            float distance = 3.5f;
            float horizontalOffset = -2.3f;

            // 计算位置
            Vec3 position = null;
            if (player != null) {
                position = soul.calculateLeftFrontPosition(player, distance, horizontalOffset);
            }

            // 更新实体位置
            soul.setPos(position.x, position.y, position.z);

            // 可选：让实体面朝玩家
            if (player != null) {
                soul.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
            }

            // 必须最后设置Owner
            soul.setOwner(player);

            if (level.addFreshEntity(soul)) {
                soulMap.put(player.getUUID(), soul);
            }
        } catch (Exception e) {
            Murasame.LOGGER.error("生成异常", e);
        }
    }

    public static void removeSoul(Player player) {
        EntityMurasameSoul soul = soulMap.remove(player.getUUID());
        if (soul != null && soul.isAlive()) {
            Murasame.LOGGER.debug("removed player soul: {}",player.getId());
            soul.discard();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        removeSoul(player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        removeSoul(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        // 使用玩家特定的设置而不是全局设置
        PlayerSoulData playerData = PlayerSoulDataManager.getOrCreatePlayerData(event.getEntity());
        boolean showMurasameSoul = playerData.shouldShowMurasameSoul();

        if (
                event.getEntity() instanceof ServerPlayer player) {
            // 每3 tick检测一次（更快响应）
            if (player.tickCount % 3 == 0) {
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                boolean shouldHave = false;
                if(stack.getItem() instanceof ItemSlashBlade && BladeStateAccess.of(stack)
                        .map(state -> "item.murasame.murasamemaru_awakened".equals(state.getTranslationKey()))
                        .orElse(false)){
                    shouldHave = true;
                }
                EntityMurasameSoul soul = soulMap.get(player.getUUID());

                if (shouldHave && showMurasameSoul) {
                    if (soul == null || !soul.isAlive() || soul.level() != player.level()) {
                        spawnSoulEntity(player);
                    }
                } else {
                    // 如果玩家没有合适的剑或选择了隐藏，则移除实体
                    if (soul != null) {
                        removeSoul(player);
                    }
                }
            }
        }
    }

    // 添加一个公共方法来强制更新特定玩家的实体状态
    public static void updateSoulState(Player player) {
        if (player.level().isClientSide() || !player.isAlive()) return;
        PlayerSoulData playerData = PlayerSoulDataManager.getOrCreatePlayerData(player);
        boolean showMurasameSoul = playerData.shouldShowMurasameSoul();

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean shouldHave = false;
        if(stack.getItem() instanceof ItemSlashBlade && BladeStateAccess.of(stack)
                .map(state -> "item.murasame.murasamemaru_awakened".equals(state.getTranslationKey()))
                .orElse(false)){
            shouldHave = true;
        }

        EntityMurasameSoul soul = soulMap.get(player.getUUID());

        if (shouldHave && showMurasameSoul) {
            // 需要显示实体但实体不存在时创建
            if (soul == null || !soul.isAlive() || soul.level() != player.level()) {
                spawnSoulEntity(player);
            }
        } else {
            // 不需要显示实体时移除
            if (soul != null) {
                removeSoul(player);
            }
        }
    }
}
