package fr.titi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = waterfall.MODID)
public class WaterfallHandler {

    private static int tickCounter = 0;
    private static final Set<BlockPos> processedThisTick = new HashSet<>();

    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        if (!Config.enabled) return;

        if (!(event.getLevel() instanceof Level level)) return;
        BlockPos pos = event.getPos();

        if (processedThisTick.contains(pos)) return;
        processedThisTick.add(pos);

        FluidState newFluidState = event.getNewState().getFluidState();

        if (newFluidState.is(Fluids.FLOWING_WATER) || newFluidState.is(Fluids.WATER)) {
            waterfall.getLogger().info("Water placed at {}", pos);
            if (WaterfallManager.isWaterfall(level, pos)) {
                int height = WaterfallManager.calculateWaterfallHeight(level, pos);
                waterfall.getLogger().info("Waterfall detected at {} with height {}", pos, height);
                if (height >= Config.maxWaterfallHeight) {
                    waterfall.getLogger().info("Blocking waterfall flow at {} (height: {})", pos, height);
                    event.setCanceled(true);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                    if (Config.enableEvaporationEffect && level instanceof ServerLevel serverLevel) {
                        WaterfallManager.spawnEvaporationEffect(serverLevel, pos);
                        WaterfallManager.addEvaporationPoint(pos);
                        waterfall.getLogger().info("Added evaporation point and spawned particles at {}", pos);
                    }
                }
            }
        }

        if (newFluidState.is(Fluids.FLOWING_LAVA) || newFluidState.is(Fluids.LAVA)) {
            waterfall.getLogger().info("Lava placed at {}", pos);
            if (LavafallManager.isLavafall(level, pos)) {
                int height = LavafallManager.calculateLavafallHeight(level, pos);
                waterfall.getLogger().info("Lavafall detected at {} with height {}", pos, height);
                if (height >= Config.maxLavafallHeight) {
                    waterfall.getLogger().info("Blocking lavafall flow at {} (height: {})", pos, height);
                    event.setCanceled(true);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                    if (Config.enableLavaEvaporationEffect && level instanceof ServerLevel serverLevel) {
                        LavafallManager.spawnEvaporationEffect(serverLevel, pos);
                        LavafallManager.addEvaporationPoint(pos);
                        waterfall.getLogger().info("Added lava evaporation point and spawned particles at {}", pos);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!Config.enabled) return;

        Level level = event.getLevel();
        if (level instanceof ServerLevel) {
            processedThisTick.clear();

            if (tickCounter % 10 == 0) {
                WaterfallManager.tickEvaporationEffects((ServerLevel) level);
                LavafallManager.tickEvaporationEffects((ServerLevel) level);
            }

            tickCounter++;
            if (tickCounter >= 100) {
                WaterfallManager.cleanupOldBlocks();
                LavafallManager.cleanupOldBlocks();
                tickCounter = 0;
            }
        }
    }
}
