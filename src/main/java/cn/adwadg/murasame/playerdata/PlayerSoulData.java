package cn.adwadg.murasame.playerdata;

import com.mojang.serialization.Codec;

public final class PlayerSoulData {
    public static final Codec<PlayerSoulData> CODEC = Codec.BOOL.xmap(PlayerSoulData::new, PlayerSoulData::shouldShowMurasameSoul);
    private boolean showMurasameSoul;
    public PlayerSoulData() { this(true); }
    private PlayerSoulData(boolean show) { showMurasameSoul = show; }
    public boolean shouldShowMurasameSoul() { return showMurasameSoul; }
    public void setShowMurasameSoul(boolean show) { showMurasameSoul = show; }
}
