package net.chris.createshufflefilterfabric.mixin;

import com.simibubi.create.content.logistics.filter.FilterItem;
import net.chris.createshufflefilterfabric.CreateShuffleFilterFabric;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FilterItem.class, remap = false)
public class MixinFilterItem {

    @Inject(method = "getFilterItems", at = @At("HEAD"), cancellable = true, remap = false)
    private static void allowShuffleFilter(ItemStack stack, CallbackInfoReturnable<ItemStackHandler> cir) {
        // Prüfen ob es unser Shuffle Filter ist
        if (stack.getItem() == CreateShuffleFilterFabric.SHUFFLE_FILTER) {
            ItemStackHandler newInv = new ItemStackHandler(18);

            // NBT laden falls vorhanden
            if (stack.hasNbt()) {
                NbtCompound invNBT = stack.getOrCreateSubNbt("Items");
                if (!invNBT.isEmpty())
                    newInv.deserializeNBT(invNBT);
            }

            cir.setReturnValue(newInv);
        }
    }
}