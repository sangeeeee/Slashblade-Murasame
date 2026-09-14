package cn.adwadg.murasame;

import cn.adwadg.murasame.Registry.ModBlocks;
import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import cn.adwadg.murasame.client.utils.MurasameEvolutionTracker;
import cn.adwadg.murasame.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Murasame.MOD_ID)
@PrefixGameTestTemplate(false)
public class PersonalStoneGameTests {
    private static ServerPlayer qualified(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.experienceLevel = ModConfig.GAINLEVELNEEDED.get() * 3;
        player.getStats().setValue(player, Stats.ENTITY_KILLED.get(EntityType.ZOMBIE), ModConfig.GAINKILLNEEDED.get());
        for (var effect : java.util.List.of(MobEffects.REGENERATION, MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE,
                MobEffects.MOVEMENT_SPEED, MobEffects.LUCK)) player.addEffect(new MobEffectInstance(effect, 200, 0));
        return player;
    }

    private static EmbeddedBladeStoneEntity stone(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, ModBlocks.EMBEDDED_STONE.get());
        return (EmbeddedBladeStoneEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static void use(EmbeddedBladeStoneEntity stone, ServerPlayer player) {
        var pos = stone.getBlockPos();
        stone.getBlockState().useWithoutItem(player.level(), player,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static int blades(ServerPlayer player) {
        return player.getInventory().items.stream().filter(MurasameEvolutionTracker::isValidBlade).mapToInt(ItemStack::getCount).sum();
    }

    @GameTest(template = "test_empty")
    public static void eachPlayerCanClaimOnce(GameTestHelper helper) {
        var first = qualified(helper);
        var second = qualified(helper);
        var stone = stone(helper, new BlockPos(1, 1, 1));
        var identity = stone.getStoneId();
        use(stone, first);
        helper.assertTrue(blades(first) == 1 && stone.hasClaimed(first.getUUID()), "First player did not receive a blade");
        helper.assertTrue(stone.viewFor(first).claimed() && !stone.viewFor(second).claimed(), "Personal views are not independent");
        int levels = first.experienceLevel;
        use(stone, first);
        helper.assertTrue(blades(first) == 1 && first.experienceLevel == levels, "Repeat claim duplicated the reward or charged XP");
        use(stone, second);
        helper.assertTrue(blades(second) == 1 && stone.hasClaimed(second.getUUID()), "Second player could not claim the same stone");
        helper.assertTrue(stone.getStoneId().equals(identity), "Claiming changed the instance identity");
        helper.assertBlockPresent(ModBlocks.EMBEDDED_STONE.get(), new BlockPos(1, 1, 1));
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void claimsSurviveBlockEntityReload(GameTestHelper helper) {
        var first = qualified(helper);
        var second = qualified(helper);
        var original = stone(helper, new BlockPos(1, 1, 1));
        use(original, first);
        var id = original.getStoneId();
        var saved = original.saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.getLevel().removeBlockEntity(original.getBlockPos());
        var restored = new EmbeddedBladeStoneEntity(original.getBlockPos(), original.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.getLevel().setBlockEntity(restored);
        helper.assertTrue(restored.getStoneId().equals(id), "Reload changed identity");
        helper.assertTrue(restored.hasClaimed(first.getUUID()) && !restored.hasClaimed(second.getUUID()), "Reload lost or shared claim state");
        use(restored, first);
        helper.assertTrue(blades(first) == 1, "Reload allowed a duplicate reward");
        var update = restored.getUpdateTag(helper.getLevel().registryAccess());
        helper.assertFalse(update.contains("ClaimedPlayers"), "Chunk packets exposed all claimants");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void controlPickDoesNotCopyClaims(GameTestHelper helper) {
        var player = qualified(helper);
        var stone = stone(helper, new BlockPos(1, 1, 1));
        use(stone, player);
        var copied = new ItemStack(ModBlocks.EMBEDDED_STONE_ITEM.get());
        stone.saveToItem(copied, helper.getLevel().registryAccess());
        helper.assertFalse(copied.has(DataComponents.BLOCK_ENTITY_DATA), "Ctrl-pick retained the instance NBT");
        helper.assertTrue(stone.hasClaimed(player.getUUID()), "Picking the block changed the original claim");
        helper.succeed();
    }

    private static EmbeddedBladeStoneEntity placeCopiedItem(GameTestHelper helper, ServerPlayer player, BlockPos pos, ItemStack item) {
        helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        var context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, item,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(((BlockItem) item.getItem()).place(context).consumesAction(), "Copied BlockItem could not be placed");
        return (EmbeddedBladeStoneEntity) helper.getLevel().getBlockEntity(pos);
    }

    @GameTest(template = "test_empty")
    public static void copiedNbtResetsAtNewAndOriginalPosition(GameTestHelper helper) {
        var player = qualified(helper);
        var source = stone(helper, new BlockPos(1, 1, 1));
        use(source, player);
        var id = source.getStoneId();
        // Deliberately emulate old/externally-created NBT items, bypassing the sanitized picker.
        var copied = new ItemStack(ModBlocks.EMBEDDED_STONE_ITEM.get());
        copied.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(source.saveWithId(helper.getLevel().registryAccess())));
        var other = placeCopiedItem(helper, player, helper.absolutePos(new BlockPos(3, 1, 1)), copied.copy());
        helper.assertFalse(other.getStoneId().equals(id), "Copied NBT reused the source identity");
        helper.assertFalse(other.hasClaimed(player.getUUID()), "New stone inherited the player's claim");
        helper.assertTrue(source.hasClaimed(player.getUUID()), "Placing a copy reset the original stone");
        var replacement = placeCopiedItem(helper, player, source.getBlockPos(), copied.copy());
        helper.assertFalse(replacement.getStoneId().equals(id), "Same-position replacement reused the old identity");
        helper.assertFalse(replacement.hasClaimed(player.getUUID()), "Same-position replacement inherited claims");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void relocatedStructureNbtGetsNewIdentity(GameTestHelper helper) {
        var player = qualified(helper);
        var source = stone(helper, new BlockPos(1, 1, 1));
        use(source, player);
        var other = stone(helper, new BlockPos(3, 1, 1));
        other.loadWithComponents(source.saveWithId(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.assertFalse(other.getStoneId().equals(source.getStoneId()), "Relocated structure reused identity");
        helper.assertFalse(other.hasClaimed(player.getUUID()), "Relocated structure copied claimants");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void destructionAndSilkTouchDropNothing(GameTestHelper helper) {
        var player = qualified(helper);
        var stone = stone(helper, new BlockPos(1, 1, 1));
        var tool = new ItemStack(Items.NETHERITE_PICKAXE);
        var level = helper.getLevel();
        helper.assertTrue(Block.getDrops(stone.getBlockState(), level, stone.getBlockPos(), stone, player, tool).isEmpty(), "Normal mining dropped items");
        tool.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        helper.assertTrue(Block.getDrops(stone.getBlockState(), level, stone.getBlockPos(), stone, player, tool).isEmpty(), "Silk touch dropped items");
        level.destroyBlock(stone.getBlockPos(), true, player);
        helper.assertTrue(level.getBlockEntity(stone.getBlockPos()) == null && level.getBlockState(stone.getBlockPos()).isAir(), "Breaking left the shared block behind");
        helper.assertFalse(stone.tryClaim(player), "Removed stone still grants claims");
        helper.succeed();
    }
}
