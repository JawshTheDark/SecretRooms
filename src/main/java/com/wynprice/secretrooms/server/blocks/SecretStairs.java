package com.wynprice.secretrooms.server.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SecretStairs extends SecretBaseBlock {
    public SecretStairs(Properties properties) {
        super(properties);
    }

    public static final EnumProperty<Direction> FACING = StairBlock.FACING;
    public static final EnumProperty<Half> HALF = StairBlock.HALF;
    public static final EnumProperty<StairsShape> SHAPE = StairBlock.SHAPE;

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        //Vanilla stairs know what shape they have, so ask them
        return Blocks.OAK_STAIRS.defaultBlockState()
            .setValue(FACING, state.getValue(FACING))
            .setValue(HALF, state.getValue(HALF))
            .setValue(SHAPE, state.getValue(SHAPE))
            .getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HALF, direction != Direction.DOWN && (direction == Direction.UP || context.getClickLocation().y - (double)pos.getY() <= 0.5D) ? Half.BOTTOM : Half.TOP);
        return state.setValue(SHAPE, getShapeProperty(state, context.getLevel(), pos));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return direction.getAxis().isHorizontal() ? state.setValue(SHAPE, getShapeProperty(state, level, pos)) : super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    private static StairsShape getShapeProperty(BlockState state, BlockGetter world, BlockPos position) {
        Direction direction = state.getValue(FACING);
        BlockState oppositeState = world.getBlockState(position.relative(direction));
        if (isBlockStairs(oppositeState) && state.getValue(HALF) == oppositeState.getValue(HALF)) {
            Direction oppositeDirection = oppositeState.getValue(FACING);
            if (oppositeDirection.getAxis() != state.getValue(FACING).getAxis() && isDifferentStairs(state, world, position, oppositeDirection.getOpposite())) {
                if (oppositeDirection == direction.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockState othersideState = world.getBlockState(position.relative(direction.getOpposite()));
        if (isBlockStairs(othersideState) && state.getValue(HALF) == othersideState.getValue(HALF)) {
            Direction othersideDirection = othersideState.getValue(FACING);
            if (othersideDirection.getAxis() != state.getValue(FACING).getAxis() && isDifferentStairs(state, world, position, othersideDirection)) {
                if (othersideDirection == direction.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }

                return StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean isDifferentStairs(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        BlockState offsetState = world.getBlockState(pos.relative(direction));
        return !isBlockStairs(offsetState) || offsetState.getValue(FACING) != state.getValue(FACING) || offsetState.getValue(HALF) != state.getValue(HALF);
    }

    public static boolean isBlockStairs(BlockState state) {
        return state.getBlock() instanceof StairBlock || state.getBlock() instanceof SecretStairs;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HALF, SHAPE);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
