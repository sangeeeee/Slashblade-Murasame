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
    private static int screenshotDelay;
    private static int startupDelay;

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
                if (++startupDelay < 40) return;
                // On subsequent runs, these come solely from persisted claims and ChunkWatchEvent.Sent.
                if (client.level.getBlockEntity(new BlockPos(2, -60, 0)) instanceof EmbeddedBladeStoneEntity previous) {
                    require(previous.isClientViewKnown() && !previous.shouldRenderBlade(), "Rejoining lost the saved personal view");
                    Murasame.LOGGER.info("CLIENT_STONE_REJOIN_PASSED");
                }
                var models = BladeModelManager.getInstance();
                for (var blade : java.util.List.of("murasamemaru", "murasamemaru_awakened")) {
                    var model = ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, "models/murasame/" + blade + ".obj");
                    var texture = ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, "models/murasame/" + blade + ".png");
                    require(models.getModel(model) != models.defaultModel, "Blade model fell back to the default: " + blade);
                    require(client.getResourceManager().getResource(texture).isPresent(), "Blade texture missing: " + blade);
                }
                CreativeModeTabs.tryRebuildTabContents(client.level.enabledFeatures(), true, client.level.registryAccess());
                long blades = mods.flammpfeil.slashblade.SlashBladeCreativeGroup.SLASHBLADE_GROUP.get().getDisplayItems().stream()
                        .filter(stack -> BladeStateAccess.of(stack).map(state -> state.getTranslationKey().startsWith("item.murasame.")).orElse(false)).count();
                require(blades == 2, "Expected both named blades in the SlashBlade creative tab, got " + blades);
                var soul = new EntityMurasameSoul(ModEntities.MURASAME_SOUL.get(), client.level);
                require(client.getEntityRenderDispatcher().getRenderer(soul) != null, "Soul renderer missing");
                var stone = new EmbeddedBladeStoneEntity(BlockPos.ZERO, ModBlocks.EMBEDDED_STONE.get().defaultBlockState());
                require(client.getBlockEntityRenderDispatcher().getRenderer(stone) != null, "Stone renderer missing");
                stone.setLevel(client.level);
                stone.applyClientView(java.util.UUID.randomUUID(), false);
                // Exercise the actual renderer, including OBJ tessellation and buffer submission.
                var buffers = client.renderBuffers().bufferSource();
                client.getBlockEntityRenderDispatcher().getRenderer(stone).render(stone, 0,
                        new com.mojang.blaze3d.vertex.PoseStack(), buffers,
                        net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
                buffers.endBatch();
                var playerId = client.player.getUUID();
                var server = client.getSingleplayerServer();
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(playerId);
                    PlayerSoulDataManager.getPlayerData(player).setShowMurasameSoul(true);
                    // Place both blocks in the disposable test world for the visual check.
                    player.serverLevel().setDayTime(6000);
                    player.serverLevel().setBlock(new BlockPos(0, -60, 0), ModBlocks.EMBEDDED_STONE.get().defaultBlockState(), 3);
                    player.serverLevel().setBlock(new BlockPos(2, -60, 0), ModBlocks.EMBEDDED_STONE.get().defaultBlockState(), 3);
                    for (var pos : java.util.List.of(new BlockPos(0, -60, 0), new BlockPos(2, -60, 0))) {
                        var placed = (EmbeddedBladeStoneEntity) player.serverLevel().getBlockEntity(pos);
                        placed.resetForPlacement();
                        placed.syncTrackingPlayers();
                        if (pos.getX() == 2) placed.tryClaim(player);
                    }
                    player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                    player.connection.teleport(3.8, -59, 5, 153, 18);
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
                    if (++screenshotDelay == 40) {
                        server.execute(() -> {
                            var level = server.overworld();
                            var pos = new BlockPos(2, -60, 0);
                            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 2);
                        });
                    }
                    if (screenshotDelay >= 60) {
                        var available = (EmbeddedBladeStoneEntity) client.level.getBlockEntity(new BlockPos(0, -60, 0));
                        var claimed = (EmbeddedBladeStoneEntity) client.level.getBlockEntity(new BlockPos(2, -60, 0));
                        require(available != null && available.isClientViewKnown() && available.shouldRenderBlade(), "Available stone view missing");
                        require(claimed != null && claimed.isClientViewKnown() && !claimed.shouldRenderBlade(), "Claimed stone view incorrect");
                        require(!available.getClientDisplayName().getString().equals(claimed.getClientDisplayName().getString()), "Personal names did not differ");
                        net.minecraft.client.Screenshot.grab(client.gameDirectory, "embedded-stone-smoke.png",
                                client.getMainRenderTarget(), message -> Murasame.LOGGER.info("Stone screenshot: {}", message.getString()));
                        Murasame.LOGGER.info("CLIENT_SMOKE_PASSED: both OBJ models, textures, creative blades, actual stone rendering, personal stone views/names after generic updates and serverbound soul-toggle payload");
                        stage = 3;
                        client.stop();
                    }
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
