package cn.adwadg.murasame.Registry;

import cn.adwadg.murasame.Murasame;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Murasame.MOD_ID);

}
