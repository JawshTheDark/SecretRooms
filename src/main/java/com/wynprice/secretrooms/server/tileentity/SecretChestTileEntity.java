package com.wynprice.secretrooms.server.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Collections;

public class SecretChestTileEntity extends SecretTileEntity implements Container, MenuProvider {
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(27, ItemStack.EMPTY);
    private int numPlayersUsing;

    public SecretChestTileEntity(BlockPos pos, BlockState state) {
        super(SecretTileEntities.SECRET_CHEST_TILE_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.clearContent();
        ContainerHelper.loadAllItems(input, this.stacks);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.stacks);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this);
            this.level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("secretroomsmod.container.secretchest.name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return ChestMenu.threeRows(id, inv, this);
    }

    @Override
    public void startOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!user.getLivingEntity().isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    @Override
    public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!user.getLivingEntity().isSpectator()) {
            --this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    private void onOpenOrClose() {
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock(), null);
        this.level.updateNeighborsAt(this.worldPosition.below(), this.getBlockState().getBlock(), null);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        boolean value = true;
        for (ItemStack stack : this.stacks) {
            value &= stack.isEmpty();
        }
        return value;
    }

    @Override
    public ItemStack getItem(int index) {
        return this.stacks.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack removed = this.stacks.get(index).split(count);
        if (!removed.isEmpty()) {
            this.setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack slot = this.stacks.get(index);
        this.stacks.set(index, ItemStack.EMPTY);
        return slot;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.stacks.set(index, stack);
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        Collections.fill(this.stacks, ItemStack.EMPTY);
    }

    public int getNumPlayersUsing() {
        return numPlayersUsing;
    }
}
