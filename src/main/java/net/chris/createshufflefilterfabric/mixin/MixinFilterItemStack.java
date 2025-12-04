package net.chris.createshufflefilterfabric.mixin;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.chris.createshufflefilterfabric.CreateShuffleFilterFabric;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FilterItemStack.class)
public abstract class MixinFilterItemStack {

    @Inject(
            method = "of(Lnet/minecraft/item/ItemStack;)Lcom/simibubi/create/content/logistics/filter/FilterItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void shuffleFilterSupport(ItemStack stack, CallbackInfoReturnable<FilterItemStack> cir) {

        // Prüfen, ob dies unser Shuffle-Filter ist
        if (stack.getItem() == CreateShuffleFilterFabric.SHUFFLE_FILTER) {

            // Entferne Enchantments / Attribute wie Create es macht
            stack.removeSubNbt("Enchantments");
            stack.removeSubNbt("AttributeModifiers");

            try {
                // Zugriff auf die innere Klasse ListFilterItemStack
                var innerClass = FilterItemStack.ListFilterItemStack.class;
                var constructor = innerClass.getDeclaredConstructor(ItemStack.class);
                constructor.setAccessible(true);

                FilterItemStack filter = (FilterItemStack) constructor.newInstance(stack);

                cir.setReturnValue(filter);
            } catch (Exception e) {
                CreateShuffleFilterFabric.LOGGER.error("Could not instantiate ListFilterItemStack for Shuffle Filter", e);
            }
        }
    }
}
