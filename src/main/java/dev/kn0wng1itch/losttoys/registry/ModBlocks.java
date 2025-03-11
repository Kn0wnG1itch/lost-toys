package dev.kn0wng1itch.losttoys.registry;

import dev.kn0wng1itch.losttoys.LostToys;
import dev.kn0wng1itch.losttoys.block.StatueBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LostToys.MODID);

    public static final RegistryObject<Block> STATUE_BLOCK = BLOCKS.register("statue_block",
            () -> new StatueBlock(BlockBehaviour.Properties.of().strength(2f)));
}