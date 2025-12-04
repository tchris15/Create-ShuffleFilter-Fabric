package net.chris.createshufflefilterfabric.mixin;

import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.filter.FilterScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FilterScreen.class, remap = false)
public class MixinFilterScreen {

    // Shadow die privaten Text-Felder
    @Shadow private Text respectDataN;
    @Shadow private Text ignoreDataN;
    @Shadow private Text respectDataDESC;
    @Shadow private Text ignoreDataDESC;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onConstructor(FilterMenu menu, PlayerInventory inv, Text title, CallbackInfo ci) {
        try {
            boolean isShuffleFilter = false;

            if (title != null) {
                String titleText = title.getString();
                if (titleText.toLowerCase().contains("shuffle")) {
                    isShuffleFilter = true;
                }
            }

            if (isShuffleFilter) {
                // Tooltips für Shuffle Mode überschreiben
                respectDataN = Text.literal("Weighted Mode");
                ignoreDataN = Text.literal("Equal Mode");

                respectDataDESC = Text.literal("Deployer in contraptions: Items chosen by stack count. All other: NBT Data is considered");
                ignoreDataDESC = Text.literal("Deployer in contraptions: Randomness ignores item quantity. All other: NBT Data is ignored");
            }
        } catch (Exception e) {
            // Fehler ignorieren - Standard-Verhalten
        }
    }
}