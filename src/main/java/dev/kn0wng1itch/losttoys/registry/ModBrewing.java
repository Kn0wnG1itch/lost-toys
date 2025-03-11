package dev.kn0wng1itch.losttoys.registry;

import dev.kn0wng1itch.losttoys.registry.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;

public class ModBrewing {
    public static void registerBrewingRecipes() {
        BrewingRecipeRegistry.addRecipe(new IBrewingRecipe() {
            @Override
            public boolean isInput(ItemStack input) {
                return PotionUtils.getPotion(input) == Potions.AWKWARD;
            }

            @Override
            public boolean isIngredient(ItemStack ingredient) {
                return ingredient.getItem() == Items.COBBLESTONE;
            }

            @Override
            public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
                ItemStack result = input.copy();
                PotionUtils.setPotion(result, ModPotions.PETRIFICATION_POTION.get());
                return result;
            }
        });
    }
}
