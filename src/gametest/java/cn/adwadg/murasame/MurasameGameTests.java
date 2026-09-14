package cn.adwadg.murasame;

import cn.adwadg.murasame.Entities.EntityMurasameSoul;
import cn.adwadg.murasame.Registry.ModBlocks;
import cn.adwadg.murasame.Registry.ModEntities;
import cn.adwadg.murasame.Registry.SERegistry;
import cn.adwadg.murasame.client.utils.MurasameBladeHelper;
import cn.adwadg.murasame.client.utils.MurasameEvolutionTracker;
import cn.adwadg.murasame.config.ModConfig;
import cn.adwadg.murasame.events.PlayerSoulHandler;
import cn.adwadg.murasame.playerdata.PlayerSoulDataManager;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Murasame.MOD_ID)
@PrefixGameTestTemplate(false)
public class MurasameGameTests {
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(Murasame.MOD_ID, path); }

    @GameTest(template = "test_empty")
    public static void blessingCooldownIsPerPlayer(GameTestHelper helper) {
        var first = helper.makeMockServerPlayerInLevel();
        var second = helper.makeMockServerPlayerInLevel();
        first.experienceLevel = second.experienceLevel = 50;
        first.setHealth(4);
        second.setHealth(4);
        var blade = MurasameBladeHelper.createMurasameBlade(first.registryAccess());
        var state = BladeStateAccess.of(blade).orElseThrow();
        state.setTranslationKey("item.murasame.murasamemaru_awakened");
        state.addSpecialEffect(SERegistry.MURASAME_BLESSING.getId());
        for (var player : java.util.List.of(first, second, first)) {
            var event = new mods.flammpfeil.slashblade.event.SlashBladeEvent.DoSlashEvent(blade, state, player, 0, false, 10,
                    mods.flammpfeil.slashblade.util.KnockBacks.cancel);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
            if (player == first && first.getPersistentData().getBoolean("tested")) {
                helper.assertTrue(event.getDamage() == 10, "Cooldown failed to prevent a second critical");
            } else {
                helper.assertTrue(event.isCritical() && event.getDamage() == 15, "Another player's cooldown blocked the blessing");
                player.getPersistentData().putBoolean("tested", true);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void spatialComboExecutesAllSlashesAndRecovery(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setPos(helper.absoluteVec(new Vec3(1, 2, 1)));
        var blade = MurasameBladeHelper.createMurasameBlade(player.registryAccess());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, blade);
        var state = BladeStateAccess.of(blade).orElseThrow();
        var combo = cn.adwadg.murasame.Registry.ComboStateRegistry.SPATIAL_SLASH.get();
        state.setComboSeq(cn.adwadg.murasame.Registry.ComboStateRegistry.SPATIAL_SLASH.getId());
        var original = new Vec3(0.2, 0, 0);
        player.setDeltaMovement(original);
        for (int tick = 0; tick <= 40; tick++) {
            state.setLastActionTime(player.level().getGameTime() - tick);
            combo.tickAction(player);
        }
        var slashes = helper.getLevel().getEntitiesOfClass(mods.flammpfeil.slashblade.entity.EntitySlashEffect.class,
                player.getBoundingBox().inflate(8));
        helper.assertTrue(slashes.size() == 10, "Spatial combo did not execute all ten slashes: " + slashes.size());
        helper.assertTrue(combo.getTimeoutMS() >= 2000, "Combo expires before recovery");
        helper.assertTrue(player.getDeltaMovement().distanceTo(original.scale(2)) < 0.001, "Spatial hold did not restore movement");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void namedBladesAndComponents(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        ItemStack blade = MurasameBladeHelper.createMurasameBlade(registries);
        var state = BladeStateAccess.of(blade).orElseThrow();
        helper.assertTrue(state.getBaseAttackModifier() == 18F && blade.getMaxDamage() == 150, "Base blade stats did not load");
        helper.assertTrue(blade.getEnchantmentLevel(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS)) == 5, "Base enchantments missing");
        ItemStack restored = ItemStack.parse(registries, blade.save(registries)).orElseThrow();
        helper.assertTrue(ItemStack.isSameItemSameComponents(blade, restored), "Blade components did not survive saving");
        var awakened = registries.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY)
                .getOrThrow(ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, id("murasamemaru_awakened"))).value().getBlade(registries);
        helper.assertTrue(BladeStateAccess.of(awakened).orElseThrow().hasSpecialEffect(SERegistry.MURASAME_BLESSING.getId()), "Awakened blessing missing");
        helper.assertTrue(BladeStateAccess.of(awakened).orElseThrow().getSlashArtsKey().equals(id("spatial_slash")), "Spatial slash missing");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void evolutionPreservesProgress(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var blade = MurasameBladeHelper.createMurasameBlade(helper.getLevel().registryAccess());
        var state = BladeStateAccess.of(blade).orElseThrow();
        state.setKillCount(ModConfig.EVKILLNEEDED.get());
        state.setProudSoulCount(ModConfig.EVPSNEEDED.get());
        state.setRefine(7);
        MurasameEvolutionTracker.checkEvolution(player, blade);
        helper.assertTrue(state.getTranslationKey().endsWith("murasamemaru_awakened"), "Blade did not awaken");
        helper.assertTrue(state.getRefine() == 7 && state.getKillCount() == ModConfig.EVKILLNEEDED.get(), "Evolution lost existing progress");
        helper.assertFalse(MurasameEvolutionTracker.isValidBlade(blade), "Blade can evolve twice");
        var restored = ItemStack.parse(player.registryAccess(), blade.save(player.registryAccess())).orElseThrow();
        helper.assertTrue(BladeStateAccess.of(restored).orElseThrow().getBaseAttackModifier() == 40F, "Awakened stats not persisted");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void evolutionRequiresBothThresholds(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var blade = MurasameBladeHelper.createMurasameBlade(player.registryAccess());
        var state = BladeStateAccess.of(blade).orElseThrow();
        state.setKillCount(ModConfig.EVKILLNEEDED.get());
        state.setProudSoulCount(0);
        MurasameEvolutionTracker.checkEvolution(player, blade);
        helper.assertTrue(MurasameEvolutionTracker.isValidBlade(blade), "Awakened without enough proud souls");
        state.setKillCount(0);
        state.setProudSoulCount(ModConfig.EVPSNEEDED.get());
        MurasameEvolutionTracker.checkEvolution(player, blade);
        helper.assertTrue(MurasameEvolutionTracker.isValidBlade(blade), "Awakened without enough kills");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void playerSettingsPersistAndStayIndependent(GameTestHelper helper) {
        var first = helper.makeMockServerPlayerInLevel();
        var second = helper.makeMockServerPlayerInLevel();
        PlayerSoulDataManager.getPlayerData(first).setShowMurasameSoul(false);
        helper.assertTrue(PlayerSoulDataManager.getPlayerData(second).shouldShowMurasameSoul(), "Player preferences leaked to another player");
        var saved = first.saveWithoutId(new CompoundTag());
        var restored = helper.makeMockServerPlayerInLevel();
        restored.load(saved);
        helper.assertFalse(PlayerSoulDataManager.getPlayerData(restored).shouldShowMurasameSoul(), "Soul preference did not survive saving");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void shrineResourcesResolve(GameTestHelper helper) {
        var level = helper.getLevel();
        var shrine = level.getStructureManager().get(id("shrine")).orElseThrow();
        helper.assertTrue(shrine.getSize().getX() > 0, "Shrine template is empty");
        helper.assertTrue(level.registryAccess().registryOrThrow(Registries.STRUCTURE).containsKey(id("shrine")), "Shrine worldgen missing");
        helper.assertTrue(level.getServer().getAdvancements().get(id("get_murasame")) != null, "Advancement missing");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void stoneGivesExactlyOneBlade(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.experienceLevel = ModConfig.GAINLEVELNEEDED.get();
        player.getStats().setValue(player, Stats.ENTITY_KILLED.get(EntityType.ZOMBIE), ModConfig.GAINKILLNEEDED.get());
        for (var effect : java.util.List.of(MobEffects.REGENERATION, MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE, MobEffects.MOVEMENT_SPEED, MobEffects.LUCK))
            player.addEffect(new MobEffectInstance(effect, 200, 0));
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.EMBEDDED_STONE.get());
        BlockPos pos = helper.absolutePos(relative);
        var state = helper.getLevel().getBlockState(pos);
        state.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        int count = player.getInventory().items.stream().filter(MurasameEvolutionTracker::isValidBlade).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(count == 1, "Stone must grant exactly one blade");
        helper.assertBlockPresent(ModBlocks.EMBEDDED_STONE.get(), relative);
        helper.assertTrue(((cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity)
                helper.getLevel().getBlockEntity(pos)).hasClaimed(player.getUUID()), "Claim was not recorded for this player");
        helper.assertTrue(!ModConfig.GAINCLEANLEVEL.get() || player.experienceLevel == 0, "Extraction did not charge the configured levels");
        var advancement = player.server.getAdvancements().get(id("get_murasame"));
        helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone(), "Extraction advancement not awarded");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void stoneRejectsUnqualifiedPlayer(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.EMBEDDED_STONE.get());
        BlockPos pos = helper.absolutePos(relative);
        helper.getLevel().getBlockState(pos).useWithoutItem(helper.getLevel(), player,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertBlockPresent(ModBlocks.EMBEDDED_STONE.get(), relative);
        helper.assertTrue(player.getInventory().isEmpty(), "Unqualified player received a blade");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void soulVisibilityAndFollowing(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setPos(helper.absoluteVec(new Vec3(1, 2, 1)));
        var blade = MurasameBladeHelper.createMurasameBlade(player.registryAccess());
        var state = BladeStateAccess.of(blade).orElseThrow();
        state.setTranslationKey("item.murasame.murasamemaru_awakened");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, blade);
        PlayerSoulHandler.updateSoulState(player);
        var souls = helper.getLevel().getEntitiesOfClass(EntityMurasameSoul.class, player.getBoundingBox().inflate(8));
        helper.assertTrue(souls.size() == 1, "Expected one soul companion");
        var soul = souls.getFirst();
        PlayerSoulHandler.updateSoulState(player);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityMurasameSoul.class, player.getBoundingBox().inflate(8)).size() == 1, "Duplicate soul spawned");
        player.setPos(player.position().add(1, 0, 0));
        soul.tick();
        helper.assertTrue(soul.position().distanceTo(soul.calculateLeftFrontPosition(player, 3.5F, -2.3F)) < 0.01, "Soul did not follow player movement");
        PlayerSoulDataManager.getPlayerData(player).setShowMurasameSoul(false);
        PlayerSoulHandler.updateSoulState(player);
        helper.assertTrue(soul.isRemoved(), "Hiding soul did not remove it");
        helper.succeed();
    }
}
