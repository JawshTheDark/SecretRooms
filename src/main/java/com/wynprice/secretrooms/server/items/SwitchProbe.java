package com.wynprice.secretrooms.server.items;

import com.wynprice.secretrooms.client.SwitchProbeTooltipComponent;
import com.wynprice.secretrooms.server.blocks.SecretBaseBlock;
import com.wynprice.secretrooms.server.data.SecretData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class SwitchProbe extends Item {

    public static final String PROBE_HIT_DATA = "probe_hit_data";

    public SwitchProbe(Properties properties) {
        super(properties);
    }

    /**
     * @return the block that has been copied into the given probe, if there is one
     */
    public static Optional<SecretData.Snapshot> getStored(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return Optional.empty();
        }
        return data.copyTag().read(PROBE_HIT_DATA, SecretData.Snapshot.CODEC);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Optional<SecretData> data = SecretBaseBlock.getMirrorData(level, context.getClickedPos());
        BlockState state = level.getBlockState(context.getClickedPos());
        ItemStack stack = context.getItemInHand();
        if(data.isPresent()) {
            Optional<SecretData.Snapshot> stored = getStored(stack);
            if(stored.isPresent()) {
                SecretData d = new SecretData(null);
                d.restore(stored.get());
                data.get().setFrom(d);

                boolean solid = d.getBlockState().canOcclude();
                if(state.hasProperty(SecretBaseBlock.SOLID) && solid != state.getValue(SecretBaseBlock.SOLID)) {
                    level.setBlockAndUpdate(context.getClickedPos(), state.setValue(SecretBaseBlock.SOLID, solid));
                }

                if(state.hasProperty(WATERLOGGED) && d.getBlockState().hasProperty(WATERLOGGED)) {
                    data.get().setBlockState(d.getBlockState().setValue(WATERLOGGED, state.getValue(WATERLOGGED)));
                }
            }
        } else {
            SecretData d = new SecretData(null);
            d.setBlockState(state);
            BlockEntity tileEntity = level.getBlockEntity(context.getClickedPos());
            d.setTileEntityNBT(tileEntity != null ? tileEntity.saveWithFullMetadata(level.registryAccess()) : null);

            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.store(PROBE_HIT_DATA, SecretData.Snapshot.CODEC, d.snapshot()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new SwitchProbeTooltipComponent(stack));
    }
}
