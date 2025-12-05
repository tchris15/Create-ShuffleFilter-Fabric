package net.chris.createshufflefilter.mixin;

import net.chris.createshufflefilter.CreateShuffleFilter;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = DeployerMovementBehaviour.class, remap = false)
public class MixinDeployerMovementBehaviour {

    @Shadow
    private DeployerFakePlayer getPlayer(MovementContext context) {
        throw new AssertionError();
    }

    @Inject(method = "tryGrabbingItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void onTryGrabbingItem(MovementContext context, CallbackInfo ci) {
        World world = context.world;
        if (world.isClient) return;

        FilterItemStack filter = context.getFilterFromBE();

        boolean isShuffleFilter = filter != null && !filter.item().isEmpty() &&
                filter.item().getItem() == CreateShuffleFilter.SHUFFLE_FILTER;

        if (!isShuffleFilter) return;

        DeployerFakePlayer player = getPlayer(context);
        if (player == null || !player.getMainHandStack().isEmpty()) return;

        Storage<ItemVariant> storage = context.contraption.getSharedInventory();
        if (storage == null) return;

        List<ItemVariant> candidates = new ArrayList<>();
        Map<ItemVariant, Long> candidateAmounts = new HashMap<>();

        for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
            ItemVariant variant = view.getResource();
            ItemStack stack = variant.toStack((int) view.getAmount());

            if (stack.isEmpty() || !filter.test(world, stack)) continue;

            if (!candidates.contains(variant)) {
                candidates.add(variant);
                candidateAmounts.put(variant, view.getAmount());
            } else {
                candidateAmounts.put(variant, candidateAmounts.get(variant) + view.getAmount());
            }
        }

        if (candidates.isEmpty()) return;

        boolean useWeightedMode = false;

        try {
            ItemStack filterItem = filter.item();
            if (filterItem.hasNbt()) {
                var nbt = filterItem.getNbt();
                if (nbt != null && nbt.contains("RespectNBT")) {
                    useWeightedMode = nbt.getBoolean("RespectNBT");
                }
            }
        } catch (Exception e) {
        }

        ItemVariant chosen;
        if (candidates.size() == 1) {
            chosen = candidates.get(0);
        } else {
            Random random = world.getRandom();

            if (useWeightedMode) {
                List<ItemVariant> weightedList = new ArrayList<>();

                for (ItemVariant candidate : candidates) {
                    long amount = candidateAmounts.get(candidate);
                    for (int i = 0; i < amount; i++) {
                        weightedList.add(candidate);
                    }
                }

                int randomIndex = random.nextInt(weightedList.size());
                chosen = weightedList.get(randomIndex);

            } else {
                int randomIndex = random.nextInt(candidates.size());
                chosen = candidates.get(randomIndex);
            }
        }

        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = storage.extract(chosen, 1, transaction);

            if (extracted > 0) {
                ItemStack extractedStack = chosen.toStack(1);
                player.setStackInHand(Hand.MAIN_HAND, extractedStack);
                transaction.commit();

                ci.cancel();
            }
        }
    }
}