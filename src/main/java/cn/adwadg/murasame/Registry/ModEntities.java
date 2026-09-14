package cn.adwadg.murasame.Registry;

import cn.adwadg.murasame.Entities.EntityMurasameSoul;
import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Murasame.MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Murasame.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmbeddedBladeStoneEntity>> EMBEDDED_STONE_ENTITY = BLOCK_ENTITIES.register("embedded_stone",
            () -> BlockEntityType.Builder.of(EmbeddedBladeStoneEntity::new, ModBlocks.EMBEDDED_STONE.get()).build(null));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMurasameSoul>> MURASAME_SOUL =
            ENTITIES.register("murasame_soul", () ->
                    EntityType.Builder.of(EntityMurasameSoul::new, MobCategory.MISC)
                            .sized(0.5f, 1.8f) // 更合适的碰撞箱
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .noSummon()
                            .noSave()
                            .fireImmune()
                            .build("murasame_soul"));
    // 在主类初始化时调用
    public static void registerAttributes(IEventBus eventBus) {
        eventBus.addListener(EventPriority.NORMAL, false, EntityAttributeCreationEvent.class,
                event -> event.put(MURASAME_SOUL.get(), EntityMurasameSoul.createAttributes().build()));
    }

}
