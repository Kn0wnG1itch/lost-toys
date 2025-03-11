package dev.kn0wng1itch.losttoys.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public class ModRegistry {

    public static void register(IEventBus eventBus) {
        ModBlocks.BLOCKS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(eventBus);
        ModEffects.MOB_EFFECTS.register(eventBus);
        ModPotions.POTIONS.register(eventBus);
        eventBus.register(ModRenderers.class);
    }
}