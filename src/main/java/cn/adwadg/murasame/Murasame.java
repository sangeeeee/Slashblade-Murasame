package cn.adwadg.murasame;

import cn.adwadg.murasame.Registry.*;
import cn.adwadg.murasame.data.ModAdvancementProvider;
import cn.adwadg.murasame.network.NetworkHandler;
import cn.adwadg.murasame.playerdata.PlayerSoulDataManager;
import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.SlashBladeCreativeGroup;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Murasame.MOD_ID)
public class Murasame {
    public static final String MOD_ID = "murasame";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MURASAME_TAB =
            CREATIVE_MODE_TABS.register("murasame_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.murasame_tab"))
                    .withTabsBefore(SlashBladeCreativeGroup.SLASHBLADE_GROUP.getId())
                    .icon(() -> {
                        ItemStack stack = new ItemStack(SlashBladeItems.SLASHBLADE.get());
                        BladeStateAccess.of(stack).ifPresent(state -> {
                            state.setModel(ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/murasame/murasamemaru_awakened.obj"));
                            state.setTexture(ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/murasame/murasamemaru_awakened.png"));
                        });
                        return stack;
                    })
                    // SlashBlade adds named blades using each definition's creativeGroup.
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.EMBEDDED_STONE_ITEM.get());
                    }).build());

    public Murasame(IEventBus modBus, ModContainer container) {
        CREATIVE_MODE_TABS.register(modBus);
        SERegistry.REGISTRY_KEY2.register(modBus);
        ComboStateRegistry.COMBO_STATES.register(modBus);
        SlashArtsRegistry.SLASH_ARTS.register(modBus);
        SoundRegistry.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModEntities.BLOCK_ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.registerAttributes(modBus);
        PlayerSoulDataManager.ATTACHMENTS.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, cn.adwadg.murasame.config.ModConfig.SPEC);
        modBus.addListener(NetworkHandler::register);
        modBus.addListener(this::gatherData);
    }

    private void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new ModAdvancementProvider(
                event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
    }

    public static void playSound(Player player, SoundEvent sound) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, player.getSoundSource(), 1.0F, 1.0F);
        }
    }
}
