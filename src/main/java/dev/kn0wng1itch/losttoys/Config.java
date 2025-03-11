package dev.kn0wng1itch.losttoys;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Config class to manage mod settings and preferences
@Mod.EventBusSubscriber(modid = LostToys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    // Forge ConfigSpec builder
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Boolean value to toggle logging for blocked entities (e.g., iron golem statues)
    private static final ForgeConfigSpec.BooleanValue LOG_BLOCKED_ENTITIES = BUILDER
            .comment("Whether to log blocked entities during rendering or transformation.")
            .define("logBlockedEntities", true);

    // Integer value for setting the duration of petrification in seconds
    private static final ForgeConfigSpec.IntValue PETRIFICATION_DURATION = BUILDER
            .comment("Duration of petrification effect (in seconds).")
            .defineInRange("petrificationDuration", 10, 1, 60);

    // String value for the texture applied to transformed entities
    private static final ForgeConfigSpec.ConfigValue<String> ENTITY_TEXTURE = BUILDER
            .comment("The texture used for entities in petrified form.")
            .define("entityTexture", "losttoys:textures/block/stone_petrified");

    // List of item resource locations that can trigger petrification
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PETRIFYING_ITEMS = BUILDER
            .comment("Items that trigger the petrification effect on entities when used.")
            .defineListAllowEmpty("petrifyingItems", List.of("minecraft:diamond"), Config::validateItemName);

    // ConfigSpec to finalize the configuration
    static final ForgeConfigSpec SPEC = BUILDER.build();

    // Variables for storing the configuration values
    public static boolean logBlockedEntities;
    public static int petrificationDuration;
    public static String entityTexture;
    public static Set<Item> petrifyingItems;

    // Method to validate if an item name is valid
    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    // Method to load the config values when the mod is initialized
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        logBlockedEntities = LOG_BLOCKED_ENTITIES.get();
        petrificationDuration = PETRIFICATION_DURATION.get();
        entityTexture = ENTITY_TEXTURE.get();

        // Convert the list of strings into a set of items
        petrifyingItems = PETRIFYING_ITEMS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }
}
