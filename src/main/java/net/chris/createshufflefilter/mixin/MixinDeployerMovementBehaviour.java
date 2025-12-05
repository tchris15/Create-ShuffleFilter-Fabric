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
        // Nur Server-Side
        World world = context.world;
        if (world.isClient) return;

        FilterItemStack filter = context.getFilterFromBE();

        // Prüfen ob es ein Shuffle Filter ist
        boolean isShuffleFilter = filter != null && !filter.item().isEmpty() &&
                filter.item().getItem() == CreateShuffleFilter.SHUFFLE_FILTER;

        if (!isShuffleFilter) return; // Normales Verhalten für andere Filter

        DeployerFakePlayer player = getPlayer(context);
        if (player == null || !player.getMainHandStack().isEmpty()) return;

        Storage<ItemVariant> storage = context.contraption.getSharedInventory();
        if (storage == null) return;

        // Sammle alle einzigartigen Items, die durch den Filter passen
        List<ItemVariant> candidates = new ArrayList<>();
        Map<ItemVariant, Long> candidateAmounts = new HashMap<>();

        // Über alle StorageViews iterieren
        for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
            ItemVariant variant = view.getResource();
            ItemStack stack = variant.toStack((int) view.getAmount());

            if (stack.isEmpty() || !filter.test(world, stack)) continue;

            // Prüfen ob wir diesen Variant schon haben
            if (!candidates.contains(variant)) {
                candidates.add(variant);
                candidateAmounts.put(variant, view.getAmount());
            } else {
                // Wenn schon vorhanden, Amount addieren (für Weighted Mode)
                candidateAmounts.put(variant, candidateAmounts.get(variant) + view.getAmount());
            }
        }

        if (candidates.isEmpty()) return; // Keine passenden Items

        // Aktuellen Mode bestimmen (UMGEKEHRTE LOGIK!)
        boolean useWeightedMode = false;  // Default: Equal Mode

        try {
            ItemStack filterItem = filter.item();
            if (filterItem.hasNbt()) {
                var nbt = filterItem.getNbt();
                if (nbt != null && nbt.contains("RespectNBT")) {
                    useWeightedMode = nbt.getBoolean("RespectNBT"); // true = weighted
                }
            }
        } catch (Exception e) {
            // Fehler ignorieren, Equal Mode nutzen
        }

        // Item auswählen
        ItemVariant chosen;
        if (candidates.size() == 1) {
            chosen = candidates.get(0);
        } else {
            Random random = world.getRandom();

            if (useWeightedMode) {
                // Weighted Mode: Basierend auf Menge gewichten
                List<ItemVariant> weightedList = new ArrayList<>();

                for (ItemVariant candidate : candidates) {
                    long amount = candidateAmounts.get(candidate);
                    // Jede Einheit erhöht die Chance
                    for (int i = 0; i < amount; i++) {
                        weightedList.add(candidate);
                    }
                }

                int randomIndex = random.nextInt(weightedList.size());
                chosen = weightedList.get(randomIndex);

            } else {
                // Equal Mode: Einfache Zufallsauswahl
                int randomIndex = random.nextInt(candidates.size());
                chosen = candidates.get(randomIndex);
            }
        }

        // Item aus Storage extrahieren (mit Transaction)
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = storage.extract(chosen, 1, transaction);

            if (extracted > 0) {
                ItemStack extractedStack = chosen.toStack(1);
                player.setStackInHand(Hand.MAIN_HAND, extractedStack);
                transaction.commit();

                // Original-Methode abbrechen
                ci.cancel();
            }
        }
    }
}