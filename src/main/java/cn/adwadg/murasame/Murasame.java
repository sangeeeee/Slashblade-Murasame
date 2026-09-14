package cn.adwadg.murasame;

import cn.adwadg.murasame.Registry.*;
import cn.adwadg.murasame.data.ModAdvancementProvider;
import cn.adwadg.murasame.network.NetworkHandler;
import cn.adwadg.murasame.playerdata.PlayerSoulDataManager;
import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.SlashBladeCreativeGroup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;

@Mod(Murasame.MOD_ID)
public class Murasame {
    public static final String MOD_ID = "murasame";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Murasame(IEventBus modBus, ModContainer container) {
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
        modBus.addListener(this::buildCreativeContents);
    }

    private void buildCreativeContents(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(SlashBladeCreativeGroup.SLASHBLADE_GROUP.getKey())) {
            event.accept(ModBlocks.EMBEDDED_STONE_ITEM.get());
        }
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
