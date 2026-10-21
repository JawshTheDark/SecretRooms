package com.wynprice.secretrooms.server.blocks;

import com.wynprice.secretrooms.server.data.SecretBlockTags;
import com.wynprice.secretrooms.server.tileentity.SecretTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class OneWayGlass extends SecretBaseBlock {

    private static final BooleanProperty NORTH = PipeBlock.NORTH;
    private static final BooleanProperty EAST = PipeBlock.EAST;
    private static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    private static final BooleanProperty WEST = PipeBlock.WEST;
    private static final BooleanProperty UP = PipeBlock.UP;
    private static final BooleanProperty DOWN = PipeBlock.DOWN;

    /**
     * Paper thin slabs on each face of the block. Only the faces that aren't glass are solid, so the occlusion
     * shape of the block is made out of these.
     */
    private static final VoxelShape[] FACE_SLABS = new VoxelShape[6];

    static {
        double t = 0.016D;
        FACE_SLABS[Direction.DOWN.ordinal()] = Block.box(0, 0, 0, 16, t, 16);
        FACE_SLABS[Direction.UP.ordinal()] = Block.box(0, 16 - t, 0, 16, 16, 16);
        FACE_SLABS[Direction.NORTH.ordinal()] = Block.box(0, 0, 0, 16, 16, t);
        FACE_SLABS[Direction.SOUTH.ordinal()] = Block.box(0, 0, 16 - t, 16, 16, 16);
        FACE_SLABS[Direction.WEST.ordinal()] = Block.box(0, 0, 0, t, 16, 16);
        FACE_SLABS[Direction.EAST.ordinal()] = Block.box(16 - t, 0, 0, 16, 16, 16);
    }

    public OneWayGlass(Properties properties) {
        super(properties);

        this.registerDefaultState(this.defaultBlockState()
            .setValue(NORTH, true)
            .setValue(EAST, true)
            .setValue(SOUTH, true)
            .setValue(WEST, true)
            .setValue(UP, true)
            .setValue(DOWN, true)
        );
    }

    @Override
    public Boolean getSolidValue() {
        return true;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        VoxelShape shape = Shapes.empty();
        for (Direction direction : Direction.values()) {
            //The glass sides let you see through, so they don't hide the blocks behind them
            if (!state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                shape = Shapes.or(shape, FACE_SLABS[direction.ordinal()]);
            }
        }
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if(player.getMainHandItem().isEmpty()) {
            worldIn.setBlock(pos, state.cycle(PipeBlock.PROPERTY_BY_DIRECTION.get(hit.getDirection())), 3);
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if(tileEntity instanceof SecretTileEntity te) {
                te.requestModelDataUpdateIfPossible();
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, worldIn, pos, player, hit);
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
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        boolean isGlass = state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side));
        boolean isInCullTag = adjacentBlockState.is(SecretBlockTags.ONE_WAY_GLASS_CULL);

        return super.skipRendering(state, adjacentBlockState, side) || (isGlass && isInCullTag);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos offFace = context.replacingClickedOnBlock() ? context.getClickedPos() : context.getClickedPos().relative(context.getClickedFace().getOpposite());
        BlockState state = context.getLevel().getBlockState(offFace);
        if(state.getBlock() == this) {
            return state;
        }
        return super.getStateForPlacement(context).setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(context.getNearestLookingDirection().getOpposite()), false);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if(tileEntity instanceof SecretTileEntity te) {
            te.requestModelDataUpdateIfPossible();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }
}
