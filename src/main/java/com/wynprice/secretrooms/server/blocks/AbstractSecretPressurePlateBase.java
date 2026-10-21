package com.wynprice.secretrooms.server.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSecretPressurePlateBase extends SecretBaseBlock {
    public AbstractSecretPressurePlateBase(Properties properties) {
        super(properties);
    }

    public int tickRate(LevelReader worldIn) {
        return 20;
    }

    @Override
    protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        int strength = this.getRedstoneStrength(state);
        if (strength > 0) {
            this.updateState(worldIn, pos, state, strength);
        }
    }

    @Override
    public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        if (!worldIn.isClientSide()) {
            this.updateState(worldIn, pos, state, this.getRedstoneStrength(state));
        }
    }

    protected void updateState(Level worldIn, BlockPos pos, BlockState state, int oldRedstoneStrength) {
        int newStrength = this.computeRedstoneStrength(worldIn, pos);
        boolean wasPowered = oldRedstoneStrength > 0;
        boolean isPowered = newStrength > 0;
        if (oldRedstoneStrength != newStrength) {
            BlockState newState = this.setRedstoneStrength(state, newStrength);
            worldIn.setBlock(pos, newState, 2);
            this.updateNeighbors(worldIn, pos);
            worldIn.setBlocksDirty(pos, state, newState);
        }

        if (!isPowered && wasPowered) {
            this.playClickOffSound(worldIn, pos);
        } else if (isPowered && !wasPowered) {
            this.playClickOnSound(worldIn, pos);
        }

        if (isPowered) {
            worldIn.scheduleTick(pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return this.getRedstoneStrength(blockState);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel worldIn, BlockPos pos, boolean movedByPiston) {
        if (!movedByPiston && this.getRedstoneStrength(state) > 0) {
            this.updateNeighbors(worldIn, pos);
        }
    }

    private void updateNeighbors(Level worldIn, BlockPos pos) {
        worldIn.updateNeighborsAt(pos, this, null);
        worldIn.updateNeighborsAt(pos.below(), this, null);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    protected abstract void playClickOnSound(LevelAccessor worldIn, BlockPos pos);

    protected abstract void playClickOffSound(LevelAccessor worldIn, BlockPos pos);

    protected abstract int computeRedstoneStrength(Level worldIn, BlockPos pos);

    protected abstract int getRedstoneStrength(BlockState state);

    protected abstract BlockState setRedstoneStrength(BlockState state, int strength);
}
