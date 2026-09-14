package cn.adwadg.murasame.playerdata;

import cn.adwadg.murasame.Murasame;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PlayerSoulDataManager {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Murasame.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerSoulData>> SOUL_DATA =
            ATTACHMENTS.register("player_soul_data", () -> AttachmentType.builder(PlayerSoulData::new)
                    .serialize(PlayerSoulData.CODEC).copyOnDeath().build());
    public static PlayerSoulData getOrCreatePlayerData(Player player) { return player.getData(SOUL_DATA); }
    public static PlayerSoulData getPlayerData(Player player) { return player.getData(SOUL_DATA); }
}
