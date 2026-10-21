package com.wynprice.secretrooms.server.tileentity;

import com.wynprice.secretrooms.server.blocks.SecretBaseBlock;
import com.wynprice.secretrooms.server.data.SecretData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SecretTileEntity extends BlockEntity {

    /**
     * What the client needs to know in order to render a secret block. This is handed to the chunk
     * mesher, so it has to be immutable.
     */
    public record RenderData(BlockState mirrorState, Optional<BlockState> mappedState) {
    }

    private final SecretData data = new SecretData(this);

    public SecretTileEntity(BlockPos pos, BlockState state) {
        super(SecretTileEntities.SECRET_TILE_ENTITY, pos, state);
    }

    public SecretTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("secret_data", SecretData.Snapshot.CODEC, this.data.snapshot());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("secret_data", SecretData.Snapshot.CODEC).ifPresent(this.data::restore);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nullable
    @Override
    public Object getRenderData() {
        if (this.isRemoved()) {
            return null;
        }
        Optional<BlockState> mapped = Optional.empty();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            if (state.getBlock() instanceof SecretBaseBlock block) {
                mapped = block.getMappedModelState(this.level, this.worldPosition, state);
            }
        }
        return new RenderData(this.data.getBlockState(), mapped);
    }

    public SecretData getData() {
        return this.data;
    }

    /**
     * Rebuilds the model of this block. Only does something on the client.
     */
    public void requestModelDataUpdateIfPossible() {
        if (this.level != null && this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 8);
        }
    }
}
