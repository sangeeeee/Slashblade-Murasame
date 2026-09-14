package cn.adwadg.murasame.Registry;
import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.Specialeffects.MurasameBlessing;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SERegistry {
    public static final DeferredRegister<SpecialEffect> REGISTRY_KEY2 = DeferredRegister.create(SpecialEffect.REGISTRY_KEY, Murasame.MOD_ID);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> MURASAME_BLESSING = REGISTRY_KEY2.register("murasame_blessing", MurasameBlessing::new);
}
