package com.wynprice.secretrooms.client;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record SwitchProbeTooltipComponent(ItemStack stack) implements TooltipComponent {
}
