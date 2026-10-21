package com.wynprice.secretrooms.server.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SecretTrapdoor extends SecretBaseBlock {

    private static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    private static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape EAST_OPEN_AABB = Block.box(0.00101D, 0.001D, 0.001D, 3.0D, 15.999D, 15.999D);
    private static final VoxelShape WEST_OPEN_AABB = Block.box(13.0D, 0.001D, 0.001D, 15.999D, 15.999D, 15.999D);
    private static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.001D, 0.001D, 0.001D, 15.999D, 15.999D, 3.0D);
    private static final VoxelShape NORTH_OPEN_AABB = Block.box(0.001D, 0.001D, 13.0D, 15.999D, 15.999D, 15.999D);
    private static final VoxelShape BOTTOM_AABB = Block.box(0.001D, 0.001D, 0.001D, 15.999D, 3.0D, 15.999D);
    private static final VoxelShape TOP_AABB = Block.box(0.001D, 13.0D, 0.001D, 15.999D, 15.999D, 15.999D);

    private final BlockSetType type;

    public SecretTrapdoor(Properties properties, BlockSetType type) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.defaultBlockState()
            .setValue(HORIZONTAL_FACING, Direction.NORTH)
            .setValue(OPEN, false)
            .setValue(HALF, Half.BOTTOM)
            .setValue(POWERED, false)
        );
    }

    @Override
    public Optional<BlockState> getMappedModelState(BlockGetter world, BlockPos pos, BlockState state) {
        return Optional.of(
                Blocks.OAK_TRAPDOOR.defaultBlockState()
                        .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING))
                        .setValue(OPEN, state.getValue(OPEN))
                        .setValue(HALF, state.getValue(HALF))
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        VoxelShape mirrorShape = super.getShape(state, worldIn, pos, context);
        VoxelShape shape;
        if (!state.getValue(OPEN)) {
            shape = state.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
        } else {
            shape = switch (state.getValue(HORIZONTAL_FACING)) {
                case SOUTH -> SOUTH_OPEN_AABB;
                case WEST -> WEST_OPEN_AABB;
                case EAST -> EAST_OPEN_AABB;
                default -> NORTH_OPEN_AABB;
            };
        }
        return Shapes.join(shape, mirrorShape, BooleanOp.AND);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return this.getShape(state, worldIn, pos, context);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return this.getShape(state, world, pos, CollisionContext.empty());
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return state.getShape(reader, pos);
    }

    @Override
    public Boolean getSolidValue() {
        return false;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN);
            default -> false;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (!this.type.canOpenByHand()) {
            return InteractionResult.PASS;
        } else {
            state = state.cycle(OPEN);
            worldIn.setBlock(pos, state, 2);
            requestModelRefresh(worldIn, pos);
            this.playSound(player, worldIn, pos, state.getValue(OPEN));
            return InteractionResult.SUCCESS;
        }
    }

    protected void playSound(@Nullable Player player, Level worldIn, BlockPos pos, boolean open) {
        worldIn.playSound(player, pos, open ? this.type.trapdoorOpen() : this.type.trapdoorClose(), SoundSource.BLOCKS, 1.0F, worldIn.getRandom().nextFloat() * 0.1F + 0.9F);
        worldIn.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!worldIn.isClientSide()) {
            boolean flag = worldIn.hasNeighborSignal(pos);
            if (flag != state.getValue(POWERED)) {
                if (state.getValue(OPEN) != flag) {
                    state = state.setValue(OPEN, flag);
                    this.playSound(null, worldIn, pos, flag);
                }
                worldIn.setBlock(pos, state.setValue(POWERED, flag), 2);
                requestModelRefresh(worldIn, pos);
                if (state.getValue(BlockStateProperties.WATERLOGGED)) {
                    worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
                }
            }

        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        Direction direction = context.getClickedFace();
        if (!context.replacingClickedOnBlock() && direction.getAxis().isHorizontal()) {
            blockstate = blockstate.setValue(HORIZONTAL_FACING, direction).setValue(HALF, context.getClickLocation().y - (double)context.getClickedPos().getY() > 0.5D ? Half.TOP : Half.BOTTOM);
        } else {
            blockstate = blockstate.setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP);
        }

        if (context.getLevel().hasNeighborSignal(context.getClickedPos())) {
            blockstate = blockstate.setValue(OPEN, Boolean.TRUE).setValue(POWERED, Boolean.TRUE);
        }

        return blockstate;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL_FACING, OPEN, HALF, POWERED);
    }

    /**
     * Whether an entity can climb on this trapdoor, see {@link com.wynprice.secretrooms.mixin.MixinLivingEntity}.
     */
    public static boolean isLadder(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(OPEN)) {
            BlockState down = world.getBlockState(pos.below());
            if (down.getBlock() == Blocks.LADDER) {
                return down.getValue(LadderBlock.FACING) == state.getValue(HORIZONTAL_FACING);
            }
        }
        return false;
    }
}
