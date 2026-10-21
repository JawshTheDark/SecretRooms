package com.wynprice.secretrooms.server.blocks;

import com.wynprice.secretrooms.server.data.SecretData;
import com.wynprice.secretrooms.server.tileentity.SecretTileEntity;
import com.wynprice.secretrooms.world.MirrorWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class SecretBaseBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty SOLID = BooleanProperty.create("solid");

    public SecretBaseBlock(Properties properties) {
        super(properties.dynamicShape());
        this.registerDefaultState(this.defaultBlockState()
            .setValue(SOLID, false)
            .setValue(BlockStateProperties.WATERLOGGED, false)
        );
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        boolean canPlace = getValue(level, pos,
            (m, reader, p) -> m.getBlock() instanceof SimpleWaterloggedBlock waterlogged && waterlogged.canPlaceLiquid(null, reader, pos, m, fluidState.getType()),
            () -> false
        );
        if (!canPlace) {
            return false;
        }

        boolean placed = SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
        if (placed) {
            getMirrorData(level, pos).ifPresent(d -> {
                BlockState mirror = d.getBlockState();
                if (mirror.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    d.setBlockState(mirror.setValue(BlockStateProperties.WATERLOGGED, true));
                }
            });
        }
        return placed;
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state) {
        ItemStack itemStack = SimpleWaterloggedBlock.super.pickupBlock(user, level, pos, state);
        if (!itemStack.isEmpty()) {
            getMirrorData(level, pos).ifPresent(d -> {
                BlockState mirror = d.getBlockState();
                if (mirror.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    d.setBlockState(mirror.setValue(BlockStateProperties.WATERLOGGED, false));
                }
            });
        }
        return itemStack;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private int calls = 0;

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        getMirrorData(level, pos).ifPresent(data -> {
            BlockState mirror = data.getBlockState();

            LevelReader world = MirrorWorld.wrap(level);
            if (!(neighbourState.getBlock() instanceof SecretBaseBlock) && calls != 3 && level instanceof LevelAccessor accessor) {
                calls++;
                BlockState neighbourNewState = neighbourState.updateShape(world, ticks, neighbourPos, direction.getOpposite(), pos, mirror, random);
                calls--;
                Block.updateOrDestroy(neighbourState, neighbourNewState, accessor, neighbourPos, 3);
            }

            BlockState newState = mirror.updateShape(world, ticks, pos, direction, neighbourPos, world.getBlockState(neighbourPos), random);
            if (newState != mirror) {
                data.setBlockState(newState);
                BlockEntity tileEntity = world.getBlockEntity(pos);
                data.setTileEntityNBT(tileEntity != null ? tileEntity.saveWithFullMetadata(level.registryAccess()) : null);
            }
        });
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    //We need to make sure stuff like fences are connected properly.
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        getMirrorState(level, pos).ifPresent(mirror -> mirror.updateNeighbourShapes(MirrorWorld.wrap(level), pos, 3));
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    // The particles (and the sound that goes with them) of a broken block are the ones of the mirrored block

    @Override
    public void spawnDestroyByEntityParticles(Level level, @Nullable Entity entity, BlockPos pos, BlockState state) {
        super.spawnDestroyByEntityParticles(level, entity, pos, getMirrorState(level, pos).orElse(state));
    }

    @Override
    public void spawnDestroyParticles(Level level, BlockPos pos, BlockState state) {
        super.spawnDestroyParticles(level, pos, getMirrorState(level, pos).orElse(state));
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return getValue(level, pos, (mirror, reader, p) -> mirror.getShadeBrightness(reader, p), () -> super.getShadeBrightness(state, level, pos));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getValue(level, pos, (mirror, reader, p) -> mirror.getShape(reader, p, context), () -> super.getShape(state, level, pos, context));
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getValue(level, pos, (mirror, reader, p) -> mirror.getBlockSupportShape(reader, p), () -> super.getBlockSupportShape(state, level, pos));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getValue(level, pos, (mirror, reader, p) -> mirror.getCollisionShape(reader, p, context), () -> super.getCollisionShape(state, level, pos, context));
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getValue(level, pos, (mirror, reader, p) -> mirror.getVisualShape(reader, p, context), () -> super.getVisualShape(state, level, pos, context));
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getValue(level, pos, (mirror, reader, p) -> mirror.getInteractionShape(reader, p), () -> super.getInteractionShape(state, level, pos));
    }

    /**
     * Whether blocks next to this one should be culled, and whether this block stops light. This is fixed per state,
     * rather than looking at the mirrored block, and is picked when the block is placed.
     */
    protected boolean isSolid(BlockState state) {
        Boolean value = this.getSolidValue();
        return value != null ? value : state.getValue(SOLID);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return this.isSolid(state) ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return !this.isSolid(state) && super.propagatesSkylightDown(state);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        Optional<BlockState> mirror = getMirrorState(level, pos);
        if (mirror.isEmpty()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        //The clamp is to make sure that if the user say copied bedrock, it can still be destroyed.
        float hardness = mirror.get().getDestroySpeed(level, pos);
        if (hardness > 5F) {
            hardness = 5F;
        } else if (hardness < 0F) {
            hardness = 0.5F;
        }
        //The block drops itself, so it never requires a tool to harvest
        return player.getDestroySpeed(mirror.get()) / hardness / 30;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOLID, BlockStateProperties.WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SecretTileEntity(pos, state);
    }

    public Optional<BlockState> getMappedModelState(BlockGetter world, BlockPos pos, BlockState state) {
        return Optional.empty();
    }

    @Nullable
    public Boolean getSolidValue() {
        return null;
    }

    public BlockState getPlaceState(BlockGetter world, BlockPos placedOnPos, BlockState placedOn, BlockState fallback) {
        boolean waterlogged = placedOn.hasProperty(BlockStateProperties.WATERLOGGED) && placedOn.getValue(BlockStateProperties.WATERLOGGED);
        return fallback
            .setValue(SOLID, placedOn.canOcclude())
            .setValue(BlockStateProperties.WATERLOGGED, waterlogged);
    }

    public static <T> T getValue(BlockGetter world, BlockPos pos, StateFunction<T> function, Supplier<T> defaultValue) {
        Optional<BlockState> mirror = getMirrorState(world, pos);
        if (mirror.isEmpty()) {
            return defaultValue.get();
        }
        return function.getValue(mirror.get(), MirrorWorld.wrap(world), pos);
    }

    public static Optional<BlockState> getMirrorState(BlockGetter world, BlockPos pos) {
        // Anything that isn't a level, such as the region that a chunk is being built from on another thread, has
        // to go through the render data, as that is the thread safe copy of what the block entity knows
        if (world != null && pos != null && !(world instanceof Level) && world.getBlockEntityRenderData(pos) instanceof SecretTileEntity.RenderData data) {
            return Optional.of(data.mirrorState());
        }
        return getMirrorData(world, pos).map(SecretData::getBlockState);
    }

    public static Optional<SecretData> getMirrorData(BlockGetter world, BlockPos pos) {
        if(world == null || pos == null) {
            return Optional.empty();
        }

        // Fixes a deadlock that occurs when there is a SecretBlock in a spawn chunk (see https://github.com/Wyn-Price/SecretRooms/pull/53)
        BlockState blockState = null;
        if (world instanceof Level level) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk != null) {
                blockState = chunk.getBlockState(pos);
            }
        } else {
            blockState = world.getBlockState(pos);
        }

        BlockEntity te = world.getBlockEntity(pos);
        return blockState != null && blockState.getBlock() instanceof SecretBaseBlock && te instanceof SecretTileEntity ?
            Optional.of(((SecretTileEntity) te).getData()) : Optional.empty();
    }

    /**
     * Makes the client rebuild the model of the block at the given position.
     */
    public static void requestModelRefresh(BlockGetter world, BlockPos pos) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof SecretTileEntity te) {
            te.requestModelDataUpdateIfPossible();
        }
    }

    public interface StateFunction<T> {
        T getValue(BlockState mirror, BlockGetter reader, BlockPos pos);
    }

}
