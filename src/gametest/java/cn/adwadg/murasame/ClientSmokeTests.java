package cn.adwadg.murasame;

import cn.adwadg.murasame.Entities.EntityMurasameSoul;
import cn.adwadg.murasame.Registry.ModEntities;
import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import cn.adwadg.murasame.Registry.ModBlocks;
import cn.adwadg.murasame.network.ToggleKeyPacket;
import cn.adwadg.murasame.playerdata.PlayerSoulDataManager;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Opt-in development check. Excluded from the normal build together with all GameTests. */
@EventBusSubscriber(modid = Murasame.MOD_ID, value = Dist.CLIENT)
public final class ClientSmokeTests {
    private static int stage;
    private static int ticks;
    private static volatile boolean response;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("murasame.clientSmokeTest")) return;
        var client = Minecraft.getInstance();
        if (stage == 3) return;
        // These screens refer only to the disposable smoke-test world.
        if (client.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen) {
            client.screen.children().stream()
                    .filter(child -> child instanceof net.minecraft.client.gui.components.Button)
                    .map(child -> (net.minecraft.client.gui.components.Button) child)
                    .filter(button -> button.getMessage().getString().equals(net.minecraft.network.chat.Component.translatable("selectWorld.backupJoinSkipButton").getString()))
                    .findFirst().ifPresent(net.minecraft.client.gui.components.Button::onPress);
        }
        if (++ticks > 2400) {
            Murasame.LOGGER.error("CLIENT_SMOKE_FAILED: timed out entering test world or receiving payload");
            stage = 3;
            client.stop();
            return;
        }
        if (client.level == null || client.player == null || client.getSingleplayerServer() == null) return;
        try {
            if (stage == 0) {
                var models = BladeModelManager.getInstance();
                for (var blade : java.util.List.of("murasamemaru", "murasamemaru_awakened")) {
                    var model = ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, "models/murasame/" + blade + ".obj");
                    var texture = ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, "models/murasame/" + blade + ".png");
                    require(models.getModel(model) != models.defaultModel, "Blade model fell back to the default: " + blade);
                    require(client.getResourceManager().getResource(texture).isPresent(), "Blade texture missing: " + blade);
                }
                CreativeModeTabs.tryRebuildTabContents(client.level.enabledFeatures(), true, client.level.registryAccess());
                long blades = Murasame.MURASAME_TAB.get().getDisplayItems().stream()
                        .filter(stack -> BladeStateAccess.of(stack).map(state -> state.getTranslationKey().startsWith("item.murasame.")).orElse(false)).count();
                require(blades == 2, "Expected both named blades in the Murasame creative tab, got " + blades);
                var soul = new EntityMurasameSoul(ModEntities.MURASAME_SOUL.get(), client.level);
                require(client.getEntityRenderDispatcher().getRenderer(soul) != null, "Soul renderer missing");
                var stone = new EmbeddedBladeStoneEntity(BlockPos.ZERO, ModBlocks.EMBEDDED_STONE.get().defaultBlockState());
                require(client.getBlockEntityRenderDispatcher().getRenderer(stone) != null, "Stone renderer missing");
                var playerId = client.player.getUUID();
                var server = client.getSingleplayerServer();
                server.execute(() -> {
                    PlayerSoulDataManager.getPlayerData(server.getPlayerList().getPlayer(playerId)).setShowMurasameSoul(true);
                    response = true;
                });
                stage = 1;
            } else if (stage == 1 && response) {
                response = false;
                PacketDistributor.sendToServer(new ToggleKeyPacket());
                stage = 2;
            } else if (stage == 2) {
                var playerId = client.player.getUUID();
                var server = client.getSingleplayerServer();
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(playerId);
                    if (player != null && !PlayerSoulDataManager.getPlayerData(player).shouldShowMurasameSoul()) response = true;
                });
                if (response) {
                    Murasame.LOGGER.info("CLIENT_SMOKE_PASSED: both OBJ models, textures, creative blades, renderers and serverbound soul-toggle payload");
                    stage = 3;
                    client.stop();
                }
            }
        } catch (Throwable error) {
            Murasame.LOGGER.error("CLIENT_SMOKE_FAILED", error);
            stage = 3;
            client.stop();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
