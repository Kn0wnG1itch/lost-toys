package dev.kn0wng1itch.losttoys.block;

import dev.kn0wng1itch.losttoys.block.entity.StatueBlockEntity;
import dev.kn0wng1itch.losttoys.registry.ModBlockEntities;
import dev.kn0wng1itch.losttoys.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StatueBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final int MAX_RANGE = 5;

    public StatueBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof StatueBlockEntity statueBlockEntity) {
            return Shapes.create(statueBlockEntity.getEntityShape());
        }
        return Shapes.block();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof StatueBlockEntity statueBlockEntity) {
            return Shapes.create(statueBlockEntity.getEntityShape());
        }
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof StatueBlockEntity statueBlockEntity) {
            statueBlockEntity.setStoredEntity(placer);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @SuppressWarnings("resource")
    public static void replaceEntityWithStatue(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            BlockPos originalPos = entity.blockPosition();
            BlockPos validPos = findValidPlacement(serverLevel, originalPos);

            if (validPos != null) {
                BlockState statueState = ModBlocks.STATUE_BLOCK.get().defaultBlockState().setValue(FACING, entity.getDirection());
                serverLevel.setBlock(validPos, statueState, 3);
                if (serverLevel.getBlockEntity(validPos) instanceof StatueBlockEntity statueBlockEntity) {
                    statueBlockEntity.setStoredEntity(entity);
                }
                entity.remove(LivingEntity.RemovalReason.DISCARDED);

                // Add particle effect
                serverLevel.sendParticles(ParticleTypes.END_ROD, validPos.getX() + 0.5, validPos.getY() + 1, validPos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.1);
            } else {
                handleFailedTransformation(entity);
            }
        }
    }

    private static BlockPos findValidPlacement(ServerLevel serverLevel, BlockPos originalPos) {
        BlockState originalState = serverLevel.getBlockState(originalPos);
        if (originalState.isAir() || originalState.canBeReplaced()) {
            return originalPos;
        }

        for (int range = 1; range <= MAX_RANGE; range++) {
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos checkPos = originalPos.offset(x, y, z);
                        BlockState checkState = serverLevel.getBlockState(checkPos);
                        if (checkState.isAir() || checkState.canBeReplaced()) {
                            return checkPos;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.STATUE_BLOCK_ENTITY.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (level1, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof StatueBlockEntity statueBlockEntity) {
                StatueBlockEntity.tick(level1, blockPos, blockState, statueBlockEntity);
            }
        };
    }

    @SuppressWarnings("resource")
    private static void handleFailedTransformation(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 3));
        entity.level().addParticle(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1, entity.getZ(), 0, 0, 0);
    }
}