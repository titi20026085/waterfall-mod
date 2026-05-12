package fr.titi;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.IntValue MAX_WATERFALL_HEIGHT;
    private static final ModConfigSpec.BooleanValue ENABLE_EVAPORATION_EFFECT;
    private static final ModConfigSpec.DoubleValue EVAPORATION_PARTICLE_DENSITY;
    private static final ModConfigSpec.IntValue MAX_LAVAFALL_HEIGHT;
    private static final ModConfigSpec.BooleanValue ENABLE_LAVA_EVAPORATION_EFFECT;

    static {
        ENABLED = BUILDER
            .comment("Enable or disable the mod entirely")
            .translation("waterfall.config.enabled")
            .define("enabled", true);

        BUILDER.push("waterfall");

        MAX_WATERFALL_HEIGHT = BUILDER
            .comment("Maximum height (in blocks) of a vertical waterfall before it evaporates")
            .translation("waterfall.config.waterfall.maxHeight")
            .defineInRange("maxHeight", 12, 1, 100);

        ENABLE_EVAPORATION_EFFECT = BUILDER
            .comment("Show cloud/splash particle effects when a waterfall evaporates")
            .translation("waterfall.config.waterfall.enableEvaporationEffect")
            .define("enableEvaporationEffect", true);

        EVAPORATION_PARTICLE_DENSITY = BUILDER
            .comment("Density of evaporation particles (0.0 = none, 1.0 = maximum)")
            .translation("waterfall.config.waterfall.particleDensity")
            .defineInRange("particleDensity", 0.5, 0.0, 1.0);

        BUILDER.pop();
        BUILDER.push("lavafall");

        MAX_LAVAFALL_HEIGHT = BUILDER
            .comment("Maximum height (in blocks) of a vertical lavafall before it evaporates")
            .translation("waterfall.config.lavafall.maxHeight")
            .defineInRange("maxHeight", 8, 1, 100);

        ENABLE_LAVA_EVAPORATION_EFFECT = BUILDER
            .comment("Show smoke/flame particle effects when a lavafall evaporates")
            .translation("waterfall.config.lavafall.enableEvaporationEffect")
            .define("enableEvaporationEffect", true);

        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enabled;
    public static int maxWaterfallHeight;
    public static boolean enableEvaporationEffect;
    public static double evaporationParticleDensity;
    public static int maxLavafallHeight;
    public static boolean enableLavaEvaporationEffect;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enabled = ENABLED.get();
        maxWaterfallHeight = MAX_WATERFALL_HEIGHT.get();
        enableEvaporationEffect = ENABLE_EVAPORATION_EFFECT.get();
        evaporationParticleDensity = EVAPORATION_PARTICLE_DENSITY.get();
        maxLavafallHeight = MAX_LAVAFALL_HEIGHT.get();
        enableLavaEvaporationEffect = ENABLE_LAVA_EVAPORATION_EFFECT.get();
    }
}
