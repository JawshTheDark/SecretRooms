package com.wynprice.secretrooms.server.blocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class SecretRedstone extends SecretBaseBlock {

    private static final IntegerProperty POWER = BlockStateProperties.POWER;

    private boolean blockedPower = false;

    private final Set<BlockPos> toUpdate = Sets.newHashSet();

    public SecretRedstone(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
    }

    private BlockState updateSurroundingRedstone(Level worldIn, BlockPos pos, BlockState state) {
        state = this.updateNeighbourRedone(worldIn, pos, state);
        List<BlockPos> list = Lists.newArrayList(this.toUpdate);
        this.toUpdate.clear();

        for(BlockPos blockpos : list) {
            worldIn.updateNeighborsAt(blockpos, this, null);
        }

        return state;
    }

    private BlockState updateNeighbourRedone(Level world, BlockPos pos, BlockState state) {
        BlockState blockstate = state;
        int power = state.getValue(POWER);
        this.blockedPower = true;
        int neighborPower = world.getBestNeighborSignal(pos);
        this.blockedPower = false;
        int maxPower = 0;
        if (neighborPower < 15) {
            for(Direction direction : Direction.values()) {
                maxPower = this.maxSignal(maxPower, world.getBlockState(pos.relative(direction)));
            }
        }

        int stateMaxPower = maxPower - 1;
        if (neighborPower > stateMaxPower) {
            stateMaxPower = neighborPower;
        }

        if (power != stateMaxPower) {
            state = state.setValue(POWER, stateMaxPower);
            if (world.getBlockState(pos) == blockstate) {
                world.setBlock(pos, state, 2);
            }

            this.toUpdate.add(pos);

            for(Direction direction : Direction.values()) {
                this.toUpdate.add(pos.relative(direction));
            }
        }

        return state;
    }

    private void notifyWireNeighborsOfStateChange(Level worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos).getBlock() == this) {
            worldIn.updateNeighborsAt(pos, this, null);

            for(Direction direction : Direction.values()) {
                worldIn.updateNeighborsAt(pos.relative(direction), this, null);
            }

        }
    }

    @Override
    protected void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock() && !worldIn.isClientSide()) {
            this.updateSurroundingRedstone(worldIn, pos, state);

            for(Direction direction : Direction.Plane.VERTICAL) {
                worldIn.updateNeighborsAt(pos.relative(direction), this, null);
            }

            for(Direction direction1 : Direction.Plane.HORIZONTAL) {
                this.notifyWireNeighborsOfStateChange(worldIn, pos.relative(direction1));
            }

            for(Direction direction2 : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos.relative(direction2);
                if (worldIn.getBlockState(blockpos).isRedstoneConductor(worldIn, blockpos)) {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.above());
                } else {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.below());
                }
            }

        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean movedByPiston) {
        if (!movedByPiston) {
            for(Direction direction : Direction.values()) {
                worldIn.updateNeighborsAt(pos.relative(direction), this, null);
            }

            this.updateSurroundingRedstone(worldIn, pos, state);

            for(Direction direction : Direction.values()) {
                this.notifyWireNeighborsOfStateChange(worldIn, pos.relative(direction));
            }
        }
    }

    private int maxSignal(int existingSignal, BlockState neighbor) {
        if (neighbor.getBlock() != this && neighbor.getBlock() != Blocks.REDSTONE_WIRE) {
            return existingSignal;
        } else {
            int i = neighbor.getValue(POWER);
            return Math.max(i, existingSignal);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!worldIn.isClientSide()) {
            if (state.canSurvive(worldIn, pos)) {
                this.updateSurroundingRedstone(worldIn, pos, state);
            } else {
                dropResources(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }

        }
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (this.blockedPower) {
            return 0;
        } else {
            return blockState.getValue(POWER);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return !this.blockedPower;
    }
}
