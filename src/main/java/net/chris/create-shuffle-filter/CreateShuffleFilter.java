package net.chris.create-shuffle-filter;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.content.logistics.filter.FilterItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateShuffleFilter implements ModInitializer {
	public static final String MOD_ID = "create-shuffle-filter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final FilterItem SHUFFLE_FILTER = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "shuffle_filter"),
            FilterItem.regular(new Item.Settings())
    );


	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

        ItemGroupEvents.modifyEntriesEvent(AllCreativeModeTabs.BASE_CREATIVE_TAB.key()).register(content -> content.add(SHUFFLE_FILTER));
	}
}