package cn.adwadg.murasame.client.utils;

import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Common-side blade factory; the package name is retained for source compatibility. */
public final class MurasameBladeHelper {
    public static final ResourceKey<SlashBladeDefinition> MURASAME = ResourceKey.create(
            SlashBladeDefinition.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("murasame", "murasamemaru"));
    public static ItemStack createMurasameBlade(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY).getOrThrow(MURASAME).value().getBlade(registries);
    }
}
