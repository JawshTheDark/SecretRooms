package com.wynprice.secretrooms.client;

import com.wynprice.secretrooms.SecretRooms7;
import com.wynprice.secretrooms.client.model.SecretUnbakedModel;
import com.wynprice.secretrooms.server.items.SecretItems;
import com.wynprice.secretrooms.server.items.SwitchProbe;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class SecretRoomsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SecretUnbakedModel.register();

        ClientTickEvents.START_CLIENT_TICK.register(TrueVisionClient::tick);

        ClientTooltipComponentCallback.EVENT.register(component ->
            component instanceof SwitchProbeTooltipComponent probe ? new SwitchProbeTooltip(probe) : null
        );

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (stack.getItem() != SecretItems.SWITCH_PROBE) {
                return;
            }

            SwitchProbe.getStored(stack).ifPresentOrElse(stored -> {
                if (Minecraft.getInstance().hasShiftDown()) {
                    BlockState state = stored.blockState();
                    lines.add(Component.translatable(SecretRooms7.MODID + ".probe.data").withStyle(ChatFormatting.BLUE));
                    lines.add(Component.translatable(SecretRooms7.MODID + ".probe.blockset", state.getBlock().getName()).withStyle(ChatFormatting.AQUA));

                    for (Property<?> property : state.getProperties()) {
                        lines.add(Component.translatable(SecretRooms7.MODID + ".probe.blockproperty", property.getName(), propertyString(property, state)).withStyle(ChatFormatting.DARK_AQUA));
                    }
                }
            }, () -> lines.add(Component.translatable(SecretRooms7.MODID + ".probe.noneset")));
        });
    }

    private static <T extends Comparable<T>> String propertyString(Property<T> property, BlockState state) {
        return property.getName(state.getValue(property));
    }
}
