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
        // Prüfen ob es unser Shuffle Filter ist
        if (stack.getItem() == CreateShuffleFilter.SHUFFLE_FILTER) {
            tooltip.add(Text.literal("Randomizes item selection from filtered").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("matches for deployers on contraptions").formatted(Formatting.GRAY));

            // Aktuellen Mode bestimmen
            boolean useWeightedMode = false;  // Default: Equal Mode

            try {
                if (stack.hasNbt()) {
                    var nbt = stack.getNbt();
                    // In 1.20.1 ist es NBT, nicht Components
                    if (nbt != null && nbt.contains("RespectNBT")) {
                        // UMGEKEHRTE LOGIK wie besprochen
                        useWeightedMode = nbt.getBoolean("RespectNBT"); // true = weighted
                    }
                }
            } catch (Exception e) {
                // Fehler ignorieren
            }

            // Aktuellen Mode anzeigen
            Text modeComponent = Text.literal("Current Mode: ").formatted(Formatting.GOLD)
                    .append(useWeightedMode
                            ? Text.literal("Weighted").formatted(Formatting.GREEN)
                            : Text.literal("Equal").formatted(Formatting.BLUE));
            tooltip.add(modeComponent);

            if (AllKeys.shiftDown()) {
                // Detaillierter Tooltip mit Shift
                tooltip.add(Text.literal("Behaviour in deployer").formatted(Formatting.GOLD));
                tooltip.add(Text.literal("• Randomly picks items that pass the filter").formatted(Formatting.GRAY));
                tooltip.add(Text.literal("• Two modes control randomness").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                tooltip.add(Text.literal("Behaviour in all other cases").formatted(Formatting.GOLD));
                tooltip.add(Text.literal("• Acts like a normal List Filter").formatted(Formatting.GRAY));
                tooltip.add(Text.literal("• Weighted Mode = use NBT Data").formatted(Formatting.GRAY));
                tooltip.add(Text.literal("• Equal Mode = ignore NBT Data").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                // Mode-Erklärungen
                tooltip.add(Text.literal("Equal Mode").formatted(Formatting.BLUE));
                tooltip.add(Text.literal("• All matches have equal chance").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                tooltip.add(Text.literal("Weighted Mode").formatted(Formatting.GREEN));
                tooltip.add(Text.literal("• More stacks = higher chance").formatted(Formatting.GRAY));
                tooltip.add(Text.empty());

                tooltip.add(Text.literal("Use the filter GUI toggle to switch between modes").formatted(Formatting.DARK_GRAY));

            } else {
                // Kurzer Hinweis ohne Shift
                tooltip.add(Text.literal("Hold ").formatted(Formatting.DARK_GRAY)
                        .append(Text.literal("[").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Shift").formatted(Formatting.GRAY))
                        .append(Text.literal("]").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(" for details").formatted(Formatting.DARK_GRAY)));
            }
        }
    }
}