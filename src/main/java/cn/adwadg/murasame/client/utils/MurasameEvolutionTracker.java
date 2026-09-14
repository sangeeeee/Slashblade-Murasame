package cn.adwadg.murasame.client.utils;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.Registry.SoundRegistry;
import cn.adwadg.murasame.config.ModConfig;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MurasameEvolutionTracker {

    // NBT标签
    private static final String EVOLVED_TAG = "murasame_evolved";

    // 进化条件

    public static void checkEvolution(Player player, ItemStack stack) {
        if (player.level().isClientSide() || !isValidBlade(stack)) return;

        BladeStateAccess.of(stack).ifPresent(state -> {
            int kills = state.getKillCount();
            int souls = state.getProudSoulCount();


            if (kills >= ModConfig.EVKILLNEEDED.get() && souls >= ModConfig.EVPSNEEDED.get()) {
                evolveBlade(player, stack, state);
            }
        });
    }

    private static void evolveBlade(Player player, ItemStack stack, ISlashBladeState state) {
        // 更新模型和属性
        state.setModel(ResourceLocation.fromNamespaceAndPath("murasame", "models/murasame/murasamemaru_awakened.obj"));
        state.setTexture(ResourceLocation.fromNamespaceAndPath("murasame", "models/murasame/murasamemaru_awakened.png"));
        state.setBaseAttackModifier(40.0f);
        state.setTranslationKey("item.murasame.murasamemaru_awakened");
        state.setSlashArtsKey(ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID,"spatial_slash"));
        state.addSpecialEffect(ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, "murasame_blessing"));

        // 添加附魔
        var enchantments = player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        stack.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 15);
        stack.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 3);
        stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.POWER), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.SMITE), 25);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.murasame.murasamemaru_awakened"));

        // 标记为已进化
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                stack, tag -> tag.putBoolean(EVOLVED_TAG, true));

        // 通知玩家
        Murasame.playSound(player,SoundRegistry.MURASAME_GREETING.get());

        player.displayClientMessage(Component.translatable("message.murasame.evolved"), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.2F);
    }

    public static boolean isValidBlade(ItemStack stack) {
        return stack.getItem() instanceof ItemSlashBlade &&
                !isEvolved(stack) &&
                BladeStateAccess.of(stack)
                        .map(state -> "item.murasame.murasamemaru".equals(state.getTranslationKey()))
                        .orElse(false);
    }

    private static boolean isEvolved(ItemStack stack) {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean(EVOLVED_TAG);
    }

}
