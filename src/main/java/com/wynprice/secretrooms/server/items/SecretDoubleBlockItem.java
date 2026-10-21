package com.wynprice.secretrooms.server.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class SecretDoubleBlockItem extends SecretBlockItem {
    public SecretDoubleBlockItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        context.getLevel().setBlock(context.getClickedPos().above(), Blocks.AIR.defaultBlockState(), 27);
        return super.placeBlock(context, state);
    }

    @Override
    protected boolean doSetBlock(Level world, BlockPos pos, BlockPos placedOn, BlockState state, BlockState placedOnState, BlockEntity placedOnTileEntity) {
        // The top half has to be an upper half straight away, or the bottom half sees that it isn't being
        // completed by anything and breaks itself
        BlockState upper = state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) ? state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER) : state;
        boolean placedLower = super.doSetBlock(world, pos, placedOn, state, placedOnState, placedOnTileEntity);
        boolean placedUpper = super.doSetBlock(world, pos.above(), placedOn, upper, placedOnState, placedOnTileEntity);
        return placedLower | placedUpper;
    }
}
