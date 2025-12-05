package net.chris.createshufflefilter.mixin;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.chris.createshufflefilter.CreateShuffleFilter;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FilterItemStack.class, remap = false)
public class MixinFilterItemStack {

    @Inject(
            method = "of(Lnet/minecraft/item/ItemStack;)Lcom/simibubi/create/content/logistics/filter/FilterItemStack;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onOf(ItemStack filter, CallbackInfoReturnable<FilterItemStack> cir) {
        // Check if this is our shuffle filter
        if (filter.getItem() == CreateShuffleFilter.SHUFFLE_FILTER) {
            try {
                // Create a ListFilterItemStack for our shuffle filter
                Class<?> listFilterClass = FilterItemStack.ListFilterItemStack.class;
                java.lang.reflect.Constructor<?> constructor = listFilterClass.getDeclaredConstructor(ItemStack.class);
                constructor.setAccessible(true);
                FilterItemStack listFilter = (FilterItemStack) constructor.newInstance(filter);
                cir.setReturnValue(listFilter);
            } catch (Exception e) {
                CreateShuffleFilter.LOGGER.error("Failed to create ListFilterItemStack for shuffle filter", e);
            }
        }
    }
}