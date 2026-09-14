package cn.adwadg.murasame.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> GAINKILLNEEDED;
    public static final ModConfigSpec.ConfigValue<Integer> GAINLEVELNEEDED;
    public static final ModConfigSpec.ConfigValue<Integer> GAINEFFECTSNEEDED;
    public static final ModConfigSpec.ConfigValue<Integer> EVKILLNEEDED;
    public static final ModConfigSpec.ConfigValue<Integer> EVPSNEEDED;
    public static final ModConfigSpec.ConfigValue<Boolean> GAINCLEANLEVEL;

    static {
        BUILDER.push("general");
        GAINKILLNEEDED = BUILDER
                .comment("拔刀需求亡灵击杀数|0~99999")
                .defineInRange("killNeeded",500,0,99999);

        GAINLEVELNEEDED = BUILDER
                .comment("拔刀所需经验等级|0~500")
                .defineInRange("levelNeeded",50,0,500);

        GAINEFFECTSNEEDED = BUILDER
                .comment("拔刀所需正面效果数|0~24")
                .defineInRange("effectsNeeded",5,0,24);

        GAINCLEANLEVEL = BUILDER
                .comment("拔刀时是否清除对应等级经验")
                .define("gainCleanLevel",true);

        EVKILLNEEDED = BUILDER
                .comment("进化所需击杀数|0~99999")
                .defineInRange("evKillNeeded",1000,0,99999);

        EVPSNEEDED = BUILDER
                .comment("进化所需耀魂数|0~99999")
                .defineInRange("evPSNeeded",50000,0,99999);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
