package dev.kn0wng1itch.losttoys;

import com.mojang.logging.LogUtils;
import dev.kn0wng1itch.losttoys.registry.ModBrewing;
import dev.kn0wng1itch.losttoys.registry.ModRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LostToys.MODID)
public class LostToys {

    public static final String MODID = "losttoys";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LostToys() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModRegistry.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        ModBrewing.registerBrewingRecipes();
    }
}