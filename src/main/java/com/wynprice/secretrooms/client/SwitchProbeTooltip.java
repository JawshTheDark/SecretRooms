package com.wynprice.secretrooms.client;

import com.wynprice.secretrooms.SecretRooms7;
import com.wynprice.secretrooms.server.items.SwitchProbe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Shows the block that is stored in a switch probe next to the "Contained Block:" text.
 */
public class SwitchProbeTooltip extends ClientTextTooltip {

    @Nullable
    private ItemStack itemToRender;
    @Nullable
    private TextureAtlasSprite spriteToRender;
    private int spriteColor = -1;

    public SwitchProbeTooltip(SwitchProbeTooltipComponent component) {
        super(Component.translatable(SecretRooms7.MODID + ".probe.containedblock").withStyle(ChatFormatting.GOLD).getVisualOrderText());

        SwitchProbe.getStored(component.stack()).ifPresent(stored -> {
            BlockState state = stored.blockState();
            Item item = state.getBlock().asItem();
            if (item != Items.AIR) {
                this.itemToRender = new ItemStack(item);
            } else {
                Minecraft mc = Minecraft.getInstance();
                this.spriteToRender = mc.getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
                BlockTintSource tint = mc.getBlockColors().getTintSource(state, 0);
                if (tint != null) {
                    this.spriteColor = tint.color(state) | 0xFF000000;
                }
            }
        });
    }

    private boolean hasImage() {
        return this.itemToRender != null || this.spriteToRender != null;
    }

    @Override
    public int getWidth(Font font) {
        return super.getWidth(font) + (this.hasImage() ? 20 : 0);
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        int xStart = x + super.getWidth(font) + 4;
        if (this.itemToRender != null) {
            graphics.item(this.itemToRender, xStart, y - 3);
        } else if (this.spriteToRender != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.spriteToRender, xStart, y - 3, 16, 16, this.spriteColor);
        }
    }
}
