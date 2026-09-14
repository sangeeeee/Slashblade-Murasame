package cn.adwadg.murasame.Registry;

import cn.adwadg.murasame.Blocks.EmbeddedBladeStoneBlock;
import cn.adwadg.murasame.Murasame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, Murasame.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Murasame.MOD_ID);

    public static final DeferredHolder<Block, Block> EMBEDDED_STONE = BLOCKS.register("embedded_stone",
            () -> new EmbeddedBladeStoneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noLootTable()));
    public static final DeferredHolder<Item, Item> EMBEDDED_STONE_ITEM = ITEMS.register("embedded_stone",
            () -> new BlockItem(EMBEDDED_STONE.get(), new Item.Properties()));


}
