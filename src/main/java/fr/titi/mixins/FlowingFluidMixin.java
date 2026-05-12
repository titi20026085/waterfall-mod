package fr.titi.mixins;

import fr.titi.Config;
import fr.titi.WaterfallManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {

    @Inject(method = "canSpreadTo", at = @At("HEAD"), cancellable = true)
    private void preventWaterfallFlow(BlockGetter blockGetter, BlockPos fromPos, BlockState fromState,
                                     Direction direction, BlockPos toPos, BlockState toState,
                                     FluidState fluidState, Fluid fluid,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enabled) return;
        
        if (blockGetter instanceof Level level) {
            if (toState.getBlock() instanceof fr.titi.WaterVaporBlock ||
                fromState.getBlock() instanceof fr.titi.WaterVaporBlock) {
                cir.setReturnValue(false);
                return;
            }

            if (toState.getBlock() instanceof fr.titi.LavaVaporBlock ||
                fromState.getBlock() instanceof fr.titi.LavaVaporBlock) {
                cir.setReturnValue(false);
                return;
            }

            if (direction == Direction.DOWN && WaterfallManager.isWaterfall(level, fromPos)) {
                int height = WaterfallManager.calculateWaterfallHeight(level, fromPos);
                if (height >= Config.maxWaterfallHeight) {
                    if (Config.enableEvaporationEffect && level instanceof ServerLevel serverLevel) {
                        WaterfallManager.spawnEvaporationEffect(serverLevel, fromPos);
                    }
                    cir.setReturnValue(false);
                }
            }

            if (direction == Direction.DOWN && fr.titi.LavafallManager.isLavafall(level, fromPos)) {
                int height = fr.titi.LavafallManager.calculateLavafallHeight(level, fromPos);
                if (height >= Config.maxLavafallHeight) {
                    if (Config.enableLavaEvaporationEffect && level instanceof ServerLevel serverLevel) {
                        fr.titi.LavafallManager.spawnEvaporationEffect(serverLevel, fromPos);
                    }
                    cir.setReturnValue(false);
                }
            }
        }
    }
}