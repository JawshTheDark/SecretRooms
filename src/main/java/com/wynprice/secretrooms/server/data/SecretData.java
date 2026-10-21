package com.wynprice.secretrooms.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynprice.secretrooms.SecretRooms7;
import com.wynprice.secretrooms.server.blocks.SecretBaseBlock;
import com.wynprice.secretrooms.server.tileentity.SecretTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SecretData {

    /**
     * The persisted form of a {@link SecretData}. The field names match the format used by earlier versions of the mod.
     */
    public record Snapshot(BlockState blockState, Optional<CompoundTag> tileData) {
        public static final Codec<Snapshot> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockState.CODEC.fieldOf("blockstate").forGetter(Snapshot::blockState),
            CompoundTag.CODEC.optionalFieldOf("tile_data").forGetter(Snapshot::tileData)
        ).apply(i, Snapshot::new));
    }

    @Nullable
    private final SecretTileEntity base;

    private BlockState blockState = Blocks.STONE.defaultBlockState();
    @Nullable private CompoundTag tileEntityNBT = null;

    private BlockEntity tileEntityCache;

    public SecretData(@Nullable SecretTileEntity base) {
        this.base = base;
    }

    public Snapshot snapshot() {
        return new Snapshot(this.getBlockState(), Optional.ofNullable(this.tileEntityNBT));
    }

    public void restore(Snapshot snapshot) {
        this.blockState = snapshot.blockState();
        this.tileEntityNBT = snapshot.tileData().filter(tag -> !tag.isEmpty()).orElse(null);
        this.tileEntityCache = null;
        this.onChanged();
    }

    public BlockEntity getTileEntityCache() {
        if(this.tileEntityNBT != null && this.tileEntityCache == null && this.base != null && this.base.getLevel() != null) {
            Level level = this.base.getLevel();
            if ((this.tileEntityCache = BlockEntity.loadStatic(this.base.getBlockPos(), this.blockState, this.tileEntityNBT, level.registryAccess())) != null) {
                this.tileEntityCache.setLevel(level);
                this.tileEntityCache.setBlockState(this.blockState);
            } else {
                SecretRooms7.LOGGER.warn("Unable to create Block Entity with id {}. Disregarding it from future references ", this.tileEntityNBT.getStringOr("id", "<unknown>"));
                this.tileEntityNBT = null;
            }
        }
        return this.tileEntityCache;
    }

    public void setFrom(SecretData data) {
        this.blockState = data.getBlockState();
        this.tileEntityNBT = data.getTileEntityNBT();
        this.tileEntityCache = null;
        this.onChanged();
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
        this.onChanged();
    }

    public BlockState getBlockState() {
        if(this.blockState.getBlock() instanceof SecretBaseBlock || (this.base != null && this.blockState.isAir())) {
            this.blockState = Blocks.STONE.defaultBlockState();
        }
        return this.blockState;
    }

    public void setTileEntityNBT(@Nullable CompoundTag tileEntityNBT) {
        if(tileEntityNBT != null && tileEntityNBT.isEmpty()) {
            this.tileEntityNBT = null;
        } else {
            this.tileEntityNBT = tileEntityNBT;
        }
        this.tileEntityCache = null;
        this.onChanged();
    }

    private void onChanged() {
        if(this.base != null && this.base.getLevel() != null) {
            Level world = this.base.getLevel();
            BlockState state = this.base.getBlockState();
            world.sendBlockUpdated(this.base.getBlockPos(), state, state, 11);
            world.getChunkSource().getLightEngine().checkBlock(this.base.getBlockPos());
        }
    }

    @Nullable
    public CompoundTag getTileEntityNBT() {
        return tileEntityNBT;
    }
}
