package net.chris.createshufflefilter.mixin;

import net.chris.createshufflefilter.CreateShuffleFilter;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.filter.FilterItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = FilterItem.class, remap = false)
public class MixinFilterItemTooltip {

    @Inject(method = "appendTooltip", at = @At("HEAD"), remap = false)
    private void addShuffleFilterTooltip(ItemStack stack, net.minecraft.world.World world,
                                         List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (stack.getItem() == CreateShuffleFilter.SHUFFLE_FILTER) {
            tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.summary.1"));
            tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.summary.2"));

            boolean useWeightedMode = false;

            try {
                if (stack.hasNbt()) {
                    var nbt = stack.getNbt();
                    if (nbt != null && nbt.contains("RespectNBT")) {
                        useWeightedMode = nbt.getBoolean("RespectNBT");
                    }
                }
            } catch (Exception e) {
                CreateShuffleFilter.LOGGER.error("Failed to read RespectNBT from Shuffle Filter", e);
            }

            Text modeComponent = Text.translatable("item.create-shuffle-filter.shuffle_filter.summary.mode").formatted(Formatting.GOLD)
                    .append(useWeightedMode
                            ? Text.translatable("item.create-shuffle-filter.shuffle_filter.summary.mode.weighted").formatted(Formatting.GREEN)
                            : Text.translatable("item.create-shuffle-filter.shuffle_filter.summary.mode.equal").formatted(Formatting.BLUE));
            tooltip.add(modeComponent);

            if (AllKeys.shiftDown()) {
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.behaviour_deployer").formatted(Formatting.GOLD));
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.behaviour_deployer.description.1").formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.behaviour_deployer.description.2").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.behaviour_other").formatted(Formatting.GOLD));
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.behaviour_other.description.1").formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.behaviour_other.description.2").formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.behaviour_other.description.3").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.equal_mode").formatted(Formatting.BLUE));
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.equal_mode.1").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.weighted_mode").formatted(Formatting.GREEN));
                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.weighted_mode.1").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                tooltip.add(Text.translatable("item.create-shuffle-filter.shuffle_filter.tooltip.controls").formatted(Formatting.DARK_GRAY));

            } else {
                tooltip.add(Text.literal("Hold ").formatted(Formatting.DARK_GRAY)
                        .append(Text.literal("[").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Shift").formatted(Formatting.GRAY))
                        .append(Text.literal("]").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(" for details").formatted(Formatting.DARK_GRAY)));
            }
        }
    }
}