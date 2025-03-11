package dev.kn0wng1itch.losttoys.registry;

import dev.kn0wng1itch.losttoys.registry.ModEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import dev.kn0wng1itch.losttoys.LostToys;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, LostToys.MODID);

    public static final RegistryObject<Potion> PETRIFICATION_POTION = POTIONS.register("petrification_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.PETRIFICATION.get(), 600)));
}
