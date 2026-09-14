package cn.adwadg.murasame.client.utils;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "murasame")
public class CombatTracker {

    private static final Map<UUID, Integer> playerCombatTimers = new HashMap<>();
    private static final int COMBAT_DURATION = 20 * 10; // 10秒战斗状态

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        playerCombatTimers.put(player.getUUID(), COMBAT_DURATION);
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            playerCombatTimers.put(player.getUUID(), COMBAT_DURATION);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        playerCombatTimers.entrySet().removeIf(entry -> {
            int newTime = entry.getValue() - 1;
            if (newTime <= 0) return true;
            entry.setValue(newTime);
            return false;
        });
    }

    public static boolean isInCombat(Player player) {
        return playerCombatTimers.containsKey(player.getUUID());
    }

    public static int getCombatTimeLeft(Player player) {
        return playerCombatTimers.getOrDefault(player.getUUID(), 0);
    }
}
