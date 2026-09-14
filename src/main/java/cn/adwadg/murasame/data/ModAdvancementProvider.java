package cn.adwadg.murasame.data;

import cn.adwadg.murasame.advancement.ModAdvancements;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.AdvancementProvider.AdvancementGenerator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {
    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                  ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new MurasameAdvancementGenerator()));
    }

    private static class MurasameAdvancementGenerator implements AdvancementGenerator {
        private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft",
                "textures/gui/advancements/backgrounds/stone.png");

        @Override
        public void generate(HolderLookup.Provider provider, Consumer<net.minecraft.advancements.AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .display(
                            SlashBladeItems.SLASHBLADE.get(),
                            Component.translatable("advancements.murasame.get_murasame.title"),
                            Component.translatable("advancements.murasame.get_murasame.description"),
                            BACKGROUND,  // 设置背景图片
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            false
                    )
                    .addCriterion("obtained_from_stone", net.minecraft.advancements.CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                    .save(consumer, ModAdvancements.MURASAME_ADVANCEMENT_ID, existingFileHelper);
        }
    }

}
