package fr.titi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LavafallManager {

    private static final Map<BlockPos, Long> activeEvaporationPoints = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Long> processedVaporColumns = new ConcurrentHashMap<>();
    private static final long VAPOR_STABILIZATION_TIME = 30000;

    public static boolean isLavafall(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        if (!fluidState.is(Fluids.FLOWING_LAVA) && !fluidState.is(Fluids.LAVA)) {
            return false;
        }

        return isOpenSpace(level, pos.north())
            && isOpenSpace(level, pos.south())
            && isOpenSpace(level, pos.east())
            && isOpenSpace(level, pos.west());
    }

    private static boolean isOpenSpace(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        if (state.getBlock() instanceof LavaVaporBlock) {
            return true;
        }

        // Only flowing lava (not source blocks) counts as open space.
        return state.isAir() || fluidState.is(Fluids.FLOWING_LAVA);
    }

    public static int calculateLavafallHeight(Level level, BlockPos startPos) {
        int height = 0;
        BlockPos currentPos = startPos;

        for (int i = 0; i < 150; i++) {
            currentPos = currentPos.above();

            if (!isLavafall(level, currentPos)) break;

            BlockState state = level.getBlockState(currentPos);
            FluidState fluidState = state.getFluidState();
            if (!fluidState.is(Fluids.FLOWING_LAVA) && !fluidState.is(Fluids.LAVA)) break;

            height++;
        }

        return height;
    }

    public static void addEvaporationPoint(BlockPos pos) {
        long currentTime = System.currentTimeMillis();

        if (processedVaporColumns.containsKey(pos)) {
            long lastProcessed = processedVaporColumns.get(pos);
            if (currentTime - lastProcessed < VAPOR_STABILIZATION_TIME) {
                return;
            }
        }

        activeEvaporationPoints.put(pos, currentTime);
        processedVaporColumns.put(pos, currentTime);
    }

    public static void tickEvaporationEffects(ServerLevel level) {
        if (!Config.enableLavaEvaporationEffect) return;

        Map<BlockPos, Long> currentPoints = new HashMap<>(activeEvaporationPoints);

        for (Map.Entry<BlockPos, Long> entry : currentPoints.entrySet()) {
            BlockPos pos = entry.getKey();
            long addedTime = entry.getValue();
            long currentTime = System.currentTimeMillis();

            BlockPos above = pos.above();
            if (isLavafall(level, above)) {
                int height = calculateLavafallHeight(level, above);
                if (height >= Config.maxLavafallHeight) {
                    if (currentTime - addedTime > 1000) {
                        spawnEvaporationEffect(level, pos);
                        activeEvaporationPoints.put(pos, currentTime);
                    }
                } else {
                    activeEvaporationPoints.remove(pos);
                    processedVaporColumns.remove(pos);
                }
            } else {
                activeEvaporationPoints.remove(pos);
                processedVaporColumns.remove(pos);
            }
        }
    }

    public static void cleanupOldBlocks() {
        long currentTime = System.currentTimeMillis();
        activeEvaporationPoints.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > 60000
        );
        processedVaporColumns.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > VAPOR_STABILIZATION_TIME
        );
    }

    public static void spawnEvaporationEffect(ServerLevel level, BlockPos pos) {
        double density = Config.evaporationParticleDensity;

        if (density > 0) {
            createLavaColumnVapor(level, pos);
        }

        int particleCount = Math.max(1, (int) (3 * density));

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = level.random.nextDouble() * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;

            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                pos.getX() + 0.5 + offsetX,
                pos.getY() + offsetY,
                pos.getZ() + 0.5 + offsetZ,
                1, 0, 0.05, 0, 0.02);

            if (level.random.nextFloat() < 0.5f) {
                level.sendParticles(ParticleTypes.LAVA,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1, 0, 0.1, 0, 0.01);
            }
        }
    }

    private static void createLavaColumnVapor(ServerLevel level, BlockPos startPos) {
        if (processedVaporColumns.containsKey(startPos)) {
            long lastProcessed = processedVaporColumns.get(startPos);
            if (System.currentTimeMillis() - lastProcessed < VAPOR_STABILIZATION_TIME) {
                return;
            }
        }

        BlockPos currentPos = startPos.below();
        boolean placedAny = false;

        for (int vaporHeight = 0; vaporHeight < 5; vaporHeight++) {
            BlockState state = level.getBlockState(currentPos);

            if (state.isAir()) {
                int opacity = Math.max(1, 5 - vaporHeight);

                level.setBlock(currentPos,
                    waterfall.LAVA_VAPOR_BLOCK.get().defaultBlockState()
                        .setValue(LavaVaporBlock.OPACITY, opacity),
                    2 | 16 | 32);

                placedAny = true;
            } else {
                break;
            }

            currentPos = currentPos.below();
        }

        if (placedAny) {
            processedVaporColumns.put(startPos, System.currentTimeMillis());
        }
    }
}
