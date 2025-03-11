package dev.kn0wng1itch.losttoys.registry;

import dev.kn0wng1itch.losttoys.LostToys;
import dev.kn0wng1itch.losttoys.block.renderer.StatueBlockRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LostToys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRenderers {

    static {
        System.out.println("ModRenderers has been loaded.");
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        System.out.println("registerRenderers method called.");
        event.registerBlockEntityRenderer(ModBlockEntities.STATUE_BLOCK_ENTITY.get(), StatueBlockRenderer::new);
        System.out.println("StatueBlockRenderer registered.");
    }
}