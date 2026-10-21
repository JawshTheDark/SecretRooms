package com.wynprice.secretrooms.server.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SecretDoor extends SecretBaseBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

    private final BlockSetType type;

    public SecretDoor(Properties properties, BlockSetType type) {
        super(properties);
        this.type = type;
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(OPEN, false)
                        .setValue(HINGE, DoorHingeSide.LEFT)
                        .setValue(POWERED, false)
                        .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    @Override
    public Optional<BlockState> getMappedModelState(BlockGetter world, BlockPos pos, BlockState state) {
        return Optional.of(
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(FACING, state.getValue(FACING))
                        .setValue(OPEN, state.getValue(OPEN))
                        .setValue(HINGE, state.getValue(HINGE))
                        .setValue(POWERED, state.getValue(POWERED))
                        .setValue(HALF, state.getValue(HALF))
        );
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return state.getShape(worldIn, pos);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return state.getShape(worldIn, pos);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return state.getShape(reader, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        boolean closed = !state.getValue(OPEN);
        boolean rightHinge = state.getValue(HINGE) == DoorHingeSide.RIGHT;

        VoxelShape mirrorShape = super.getShape(state, worldIn, pos, context);
        VoxelShape shape = switch (direction) {
            case SOUTH -> closed ? SOUTH_AABB : (rightHinge ? EAST_AABB : WEST_AABB);
            case WEST -> closed ? WEST_AABB : (rightHinge ? SOUTH_AABB : NORTH_AABB);
            case NORTH -> closed ? NORTH_AABB : (rightHinge ? WEST_AABB : EAST_AABB);
            default -> closed ? EAST_AABB : (rightHinge ? NORTH_AABB : SOUTH_AABB);
        };
        return Shapes.join(shape, mirrorShape, BooleanOp.AND);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighbourState.getBlock() == this && neighbourState.getValue(HALF) != half ?
                state.setValue(FACING, neighbourState.getValue(FACING)).setValue(OPEN, neighbourState.getValue(OPEN)).setValue(HINGE, neighbourState.getValue(HINGE)).setValue(POWERED, neighbourState.getValue(POWERED)) :
                Blocks.AIR.defaultBlockState();
        } else {
            return half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
        }
    }

    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (!worldIn.isClientSide() && player.isCreative()) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = worldIn.getBlockState(otherPos);
            Optional<BlockState> mirror = getMirrorState(worldIn, pos);
            if (otherState.getBlock() == this && otherState.getValue(HALF) != half) {
                worldIn.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                mirror.ifPresent(blockState -> worldIn.levelEvent(player, 2001, otherPos, Block.getId(blockState)));
            }
        }
        return super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN);
            default -> false;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        if (blockpos.getY() < world.getMaxY() && world.getBlockState(blockpos.above()).canBeReplaced(context)) {
            boolean powered = world.hasNeighborSignal(blockpos) || world.hasNeighborSignal(blockpos.above());
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HINGE, this.getHingeSide(context)).setValue(POWERED, powered).setValue(OPEN, powered).setValue(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        worldIn.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    private DoorHingeSide getHingeSide(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getHorizontalDirection();
        BlockPos above = pos.above();
        Direction left = direction.getCounterClockWise();
        BlockPos leftPos = pos.relative(left);
        BlockState leftState = level.getBlockState(leftPos);
        BlockPos leftAbovePos = above.relative(left);
        BlockState leftAboveState = level.getBlockState(leftAbovePos);
        Direction right = direction.getClockWise();
        BlockPos rightPos = pos.relative(right);
        BlockState rightState = level.getBlockState(rightPos);
        BlockPos rightAbovePos = above.relative(right);
        BlockState rightAboveState = level.getBlockState(rightAbovePos);
        int balance = (leftState.isCollisionShapeFullBlock(level, leftPos) ? -1 : 0) + (leftAboveState.isCollisionShapeFullBlock(level, leftAbovePos) ? -1 : 0) + (rightState.isCollisionShapeFullBlock(level, rightPos) ? 1 : 0) + (rightAboveState.isCollisionShapeFullBlock(level, rightAbovePos) ? 1 : 0);
        boolean doorLeft = leftState.getBlock() == this && leftState.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean doorRight = rightState.getBlock() == this && rightState.getValue(HALF) == DoubleBlockHalf.LOWER;
        if ((!doorLeft || doorRight) && balance <= 0) {
            if ((!doorRight || doorLeft) && balance >= 0) {
                int stepX = direction.getStepX();
                int stepZ = direction.getStepZ();
                Vec3 click = context.getClickLocation();
                double x = click.x - (double)pos.getX();
                double z = click.z - (double)pos.getZ();
                return (stepX >= 0 || !(z < 0.5D)) && (stepX <= 0 || !(z > 0.5D)) && (stepZ >= 0 || !(x > 0.5D)) && (stepZ <= 0 || !(x < 0.5D)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            } else {
                return DoorHingeSide.LEFT;
            }
        } else {
            return DoorHingeSide.RIGHT;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (!this.type.canOpenByHand()) {
            return InteractionResult.PASS;
        } else {
            BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();

            boolean rotateClockwise = (state.getValue(HINGE) == DoorHingeSide.LEFT) == (state.getValue(OPEN));
            Rotation rotation = rotateClockwise ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;

            getMirrorData(worldIn, pos).ifPresent(d -> d.setBlockState(d.getBlockState().rotate(rotation)));
            getMirrorData(worldIn, other).ifPresent(d -> d.setBlockState(d.getBlockState().rotate(rotation)));

            state = state.cycle(OPEN);
            worldIn.setBlock(pos, state, 10);
            this.playSound(player, worldIn, pos, state.getValue(OPEN));
            worldIn.gameEvent(player, this.isOpen(state) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            requestModelRefresh(worldIn, pos);
            requestModelRefresh(worldIn, other);

            return InteractionResult.SUCCESS;
        }
    }

    public boolean isOpen(BlockState state) {
        return state.getValue(OPEN);
    }

    @Override
    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean flag = worldIn.hasNeighborSignal(pos) || worldIn.hasNeighborSignal(pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
        if (blockIn != this && flag != state.getValue(POWERED)) {
            if (flag != state.getValue(OPEN)) {
                this.playSound(null, worldIn, pos, flag);
                requestModelRefresh(worldIn, pos);
                requestModelRefresh(worldIn, state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below());
            }
            worldIn.setBlock(pos, state.setValue(POWERED, flag).setValue(OPEN, flag), 2);
        }

    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return blockstate.isFaceSturdy(worldIn, blockpos, Direction.UP);
        } else {
            return blockstate.getBlock() == this;
        }
    }

    private void playSound(@Nullable Entity entity, Level level, BlockPos pos, boolean open) {
        level.playSound(entity, pos, open ? this.type.doorOpen() : this.type.doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirrorIn) {
        return mirrorIn == Mirror.NONE ? state : state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(pos.getX(), pos.below(state.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF, FACING, OPEN, HINGE, POWERED);
    }
}
