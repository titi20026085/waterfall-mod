package fr.titi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterVaporBlock extends Block {
    public static final IntegerProperty OPACITY = IntegerProperty.create("opacity", 1, 5);

    public WaterVaporBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            .replaceable()
            .noLootTable()
            .strength(0.0F)
            .sound(net.minecraft.world.level.block.SoundType.EMPTY)
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(OPACITY, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPACITY);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (level.isClientSide()) return;

        BlockPos top = pos;
        while (level.getBlockState(top.above()).getBlock() instanceof WaterVaporBlock) {
            top = top.above();
        }

        var fluidAbove = level.getBlockState(top.above()).getFluidState();
        boolean hasWater = fluidAbove.is(Fluids.FLOWING_WATER) || fluidAbove.is(Fluids.WATER);

        if (!hasWater) {
            BlockPos current = top;
            while (level.getBlockState(current).getBlock() instanceof WaterVaporBlock) {
                level.setBlock(current, Blocks.AIR.defaultBlockState(), 3);
                current = current.below();
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        if (context == null) {
            return true;
        }

        if (context.getItemInHand().getItem() instanceof net.minecraft.world.item.BucketItem) {
            return false;
        }
        return true;
    }

    public boolean canBeReplacedByFluid(BlockState state, net.minecraft.world.level.material.FluidState fluidState) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int opacity = state.getValue(OPACITY);
        int count = opacity + random.nextInt(2);

        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.1 + random.nextDouble() * 0.8;
            double y = pos.getY() + random.nextDouble() * 0.9;
            double z = pos.getZ() + 0.1 + random.nextDouble() * 0.8;

            double vx = (random.nextDouble() - 0.5) * 0.015;
            double vy = 0.01 + random.nextDouble() * 0.02;
            double vz = (random.nextDouble() - 0.5) * 0.015;

            level.addParticle(ParticleTypes.CLOUD, x+0.5, y+4, z+0.5, vx, vy-0.2, vz);
            level.addParticle(ParticleTypes.CLOUD, x+0.5, y+4, z-0.5, vx, vy-0.2, vz);
            level.addParticle(ParticleTypes.CLOUD, x-0.5, y+4, z-0.5, vx, vy-0.2, vz);
            level.addParticle(ParticleTypes.CLOUD, x-0.5, y+4, z+0.5, vx, vy-0.2, vz);

            if (opacity >= 3 && random.nextFloat() < 0.3f) {
                level.addParticle(ParticleTypes.SPLASH, x, y, z, vx, vy * 0.5, vz);
            }
        }
    }

}
