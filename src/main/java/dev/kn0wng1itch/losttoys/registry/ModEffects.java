package dev.kn0wng1itch.losttoys.registry;

import dev.kn0wng1itch.losttoys.LostToys;
import dev.kn0wng1itch.losttoys.effect.PetrificationEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, LostToys.MODID);

    public static final RegistryObject<MobEffect> PETRIFICATION =
            MOB_EFFECTS.register("petrification", PetrificationEffect::new);
}