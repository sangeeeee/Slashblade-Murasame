package cn.adwadg.murasame.Specialeffects;

import cn.adwadg.murasame.Murasame;
import cn.adwadg.murasame.Registry.SERegistry;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Murasame.MOD_ID)
public class MurasameBlessing extends SpecialEffect {
    private static final String CRIT_READY = "murasame:blessing_crit_ready";
    public MurasameBlessing() { super(50, false, false); }

    private static boolean active(SlashBladeEvent event, LivingEntity user) {
        return !user.level().isClientSide()
                && "item.murasame.murasamemaru_awakened".equals(event.getSlashBladeState().getTranslationKey())
                && event.getSlashBladeState().hasSpecialEffect(SERegistry.MURASAME_BLESSING.getId())
                && SpecialEffect.isEffective(SERegistry.MURASAME_BLESSING.get(), user instanceof Player player ? player.experienceLevel : 50);
    }

    @SubscribeEvent
    public static void onPlayerTick(SlashBladeEvent.UpdateEvent event) {
        if (!(event.getEntity() instanceof Player player) || !event.isSelected() || !active(event, player)) return;
        if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE))
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 2, false, false));
        if (!player.hasEffect(MobEffects.REGENERATION))
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, false));
        if (!player.hasEffect(MobEffects.DAMAGE_BOOST))
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false));
    }

    @SubscribeEvent
    public static void onSlashHit(SlashBladeEvent.HitEvent event) {
        LivingEntity user = event.getUser();
        if (active(event, user) && user.getHealth() <= user.getMaxHealth() * 0.4F)
            applyKnockback(user, event.getTarget(), 2F - user.getHealth() / user.getMaxHealth() * 2F);
    }

    @SubscribeEvent
    public static void onSlash(SlashBladeEvent.DoSlashEvent event) {
        LivingEntity user = event.getUser();
        if (!active(event, user)) return;
        long now = user.level().getGameTime();
        if (user.getHealth() <= user.getMaxHealth() * 0.4F && now >= user.getPersistentData().getLong(CRIT_READY)) {
            event.getSlashBladeState().setColorCode(8388736);
            event.setCritical(true);
            event.setDamage(event.getDamage() * 1.5F);
            user.getPersistentData().putLong(CRIT_READY, now + 200);
        } else {
            event.getSlashBladeState().setColorCode(65350);
        }
    }

    public static void applyKnockback(LivingEntity attacker, LivingEntity target, float strength) {
        Vec3 direction = target.position().subtract(attacker.position()).multiply(1, 0, 1);
        if (direction.lengthSqr() < 1.0E-6) direction = attacker.getLookAngle().multiply(1, 0, 1);
        direction = direction.normalize();
        target.setDeltaMovement(target.getDeltaMovement().add(direction.x * strength, 0.3, direction.z * strength));
        target.hasImpulse = true;
    }
}
