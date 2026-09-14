package cn.adwadg.murasame.Registry;

import cn.adwadg.murasame.Murasame;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SlashArtsRegistry {
    public static final DeferredRegister<SlashArts> SLASH_ARTS =
            DeferredRegister.create(SlashArts.REGISTRY_KEY, Murasame.MOD_ID);

    public static final DeferredHolder<SlashArts, SlashArts> SPATIAL_SLASH =
            SLASH_ARTS.register("spatial_slash",
                    () -> new SlashArts((e) -> ComboStateRegistry.SPATIAL_SLASH.getId()));
}
