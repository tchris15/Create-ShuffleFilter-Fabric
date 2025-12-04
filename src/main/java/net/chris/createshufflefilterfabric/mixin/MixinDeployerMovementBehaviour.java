package net.chris.createshufflefilterfabric.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import net.chris.createshufflefilterfabric.CreateShuffleFilterFabric;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.item.ItemHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
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

        // Prüfen, ob es ein Shuffle Filter ist
        boolean isShuffleFilter = filter != null && !filter.item().isEmpty() &&
                filter.item().getItem() == CreateShuffleFilterFabric.SHUFFLE_FILTER;

        if (!isShuffleFilter) return; // Normales Verhalten für andere Filter

        DeployerFakePlayer player = getPlayer(context);
        if (player == null || !player.getMainHandStack().isEmpty()) return;

        //ItemStackHandler inv = context.contraption.getSharedInventory();
        Contraption.ContraptionInvWrapper inv = context.contraption.getSharedInventory();
        if (inv == null) return;
        //meine version, da ja itemstackhandler nicht geht
        List<StorageView<ItemVariant>> slots = new ArrayList<>();
        inv.iterator().forEachRemaining(slots::add);

        // Sammle alle einzigartigen Items, die durch den Filter passen
        List<ItemStack> candidates = new ArrayList<>();


        //meine version, da ja itemstackhandler nicht geht
        for (StorageView<ItemVariant> view : inv.nonEmptyViews()) {
            ItemVariant variant = view.getResource();
            long amount = view.getAmount();

            // Prüfen, ob die View leer ist (optional, nonEmptyViews() filtert das schon)
            if (amount == 0) continue;

            ItemStack s = variant.toStack((int) amount); // oder wie du ItemVariant → ItemStack machst
            if (!filter.test(world, s)) continue;

            // Prüfen, ob wir dieses Item schon haben
            boolean found = false;
            for (ItemStack c : candidates) {
                if (ItemStack.areEqual(c, s)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                candidates.add(s.copy());
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
        ItemStack chosen;
        if (candidates.size() == 1) {
            chosen = candidates.get(0);
        } else {
            Random random = world.getRandom();

            if (useWeightedMode) {
                // Weighted Mode: Stacks zählen
                Map<ItemStack, Integer> stackCounts = new HashMap<>();

                for (ItemStack candidate : candidates) {
                    int count = 0;

                    for (StorageView<ItemVariant> view : inv.nonEmptyViews()) {
                        ItemStack s = view.getResource().toStack((int)view.getAmount());
                        if (!s.isEmpty() && ItemStack.areEqual(s, candidate)) {
                            count++;
                        }
                    }
                    stackCounts.put(candidate, count);
                }

                // Weighted Liste erstellen
                List<ItemStack> weightedList = new ArrayList<>();
                for (Map.Entry<ItemStack, Integer> entry : stackCounts.entrySet()) {
                    for (int i = 0; i < entry.getValue(); i++) {
                        weightedList.add(entry.getKey());
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

        // Item aus Inventar nehmen
        ItemStack held = ItemHelper.extract(inv, stack -> ItemStack.areEqual(stack, chosen),
                1, false);
        player.setStackInHand(Hand.MAIN_HAND, held);

        // Original-Methode abbrechen
        ci.cancel();
    }
}