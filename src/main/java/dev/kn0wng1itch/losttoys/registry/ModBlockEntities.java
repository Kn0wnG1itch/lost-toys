package dev.kn0wng1itch.losttoys.registry;

import dev.kn0wng1itch.losttoys.LostToys;
import dev.kn0wng1itch.losttoys.block.entity.StatueBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LostToys.MODID);

    public static final RegistryObject<BlockEntityType<StatueBlockEntity>> STATUE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("statue_block_entity", () ->
                    BlockEntityType.Builder.of(StatueBlockEntity::new, ModBlocks.STATUE_BLOCK.get()).build(null));
}