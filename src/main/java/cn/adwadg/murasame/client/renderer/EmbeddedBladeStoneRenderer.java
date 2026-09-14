package cn.adwadg.murasame.client.renderer;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.TileEntity.BlockEntity.EmbeddedBladeStoneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class EmbeddedBladeStoneRenderer implements BlockEntityRenderer<EmbeddedBladeStoneEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            Murasame.MOD_ID, "models/murasame/murasamemaru.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Murasame.MOD_ID, "models/murasame/murasamemaru.png");
    private static final float MODEL_SCALE = 0.00635F;

    public EmbeddedBladeStoneRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EmbeddedBladeStoneEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Level level = entity.getLevel();
        if (level == null) return;
        ItemStack blade = entity.getBladeStack();
        if (blade.isEmpty()) return;

        var model = BladeModelManager.getInstance().getModel(MODEL);
        var lightPos = entity.getBlockPos().above();
        int light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, lightPos),
                level.getBrightness(LightLayer.SKY, lightPos));

        poseStack.pushPose();
        try {
            // Match the 1.1.4-fix placement: the OBJ blade points along -X;
            // rotating around +Z puts the tip into the stone and the grip above it.
            poseStack.translate(0.5, 1.8, 0.5);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
            BladeRenderState.renderOverrided(blade, model, "blade", TEXTURE, poseStack, buffer, light);
            // Optional luminous group supplied by compatible replacement resource packs.
            BladeRenderState.renderOverridedLuminous(blade, model, "blade_luminous", TEXTURE, poseStack, buffer, light);
        } finally {
            BladeRenderState.resetCol();
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(EmbeddedBladeStoneEntity entity) {
        // The hilt extends above the block's normal one-block render bounds.
        return true;
    }
}
