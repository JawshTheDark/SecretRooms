package com.wynprice.secretrooms.server.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GhostBlock extends SecretBaseBlock {
    public static final BooleanProperty NORMAL_CUBE = BooleanProperty.create("normal_cube");

    public GhostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(NORMAL_CUBE, false));
    }

    @Override
    public BlockState getPlaceState(BlockGetter world, BlockPos placedOnPos, BlockState placedOn, BlockState fallback) {
        return super.getPlaceState(world, placedOnPos, placedOn, fallback).setValue(NORMAL_CUBE, placedOn.isCollisionShapeFullBlock(world, placedOnPos));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORMAL_CUBE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return (adjacent.getBlock() == this && adjacent.getValue(NORMAL_CUBE)) || super.skipRendering(state, adjacent, side);
    }
}
