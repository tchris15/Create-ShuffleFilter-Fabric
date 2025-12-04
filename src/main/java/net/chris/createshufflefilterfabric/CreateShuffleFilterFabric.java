package net.chris.createshufflefilterfabric;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateShuffleFilterFabric implements ModInitializer {
	public static final String MOD_ID = "create-shuffle-filter-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item SHUFFLE_FILTER = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "shuffle_filter"),
            new Item(new FabricItemSettings())
    );

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
	}
}