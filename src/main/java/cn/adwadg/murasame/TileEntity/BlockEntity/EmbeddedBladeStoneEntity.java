package cn.adwadg.murasame.TileEntity.BlockEntity;

import cn.adwadg.murasame.Registry.ModEntities;
import cn.adwadg.murasame.client.utils.MurasameBladeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EmbeddedBladeStoneEntity extends BlockEntity {
    public EmbeddedBladeStoneEntity(BlockPos pos, BlockState state) {
        super(ModEntities.EMBEDDED_STONE_ENTITY.get(), pos, state);
    }
    public ItemStack getBladeStack() {
        return level == null ? ItemStack.EMPTY : MurasameBladeHelper.createMurasameBlade(level.registryAccess());
    }
}
