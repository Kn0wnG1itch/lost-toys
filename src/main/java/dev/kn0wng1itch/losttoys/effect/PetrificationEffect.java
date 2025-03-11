package dev.kn0wng1itch.losttoys.effect;

import dev.kn0wng1itch.losttoys.block.StatueBlock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.HashSet;
import java.util.Set;

public class PetrificationEffect extends MobEffect {

    private static final Set<EntityType<?>> BLACKLISTED_ENTITIES = new HashSet<>();

    static {
        // Add entities to the blacklist (e.g., players)
        BLACKLISTED_ENTITIES.add(EntityType.PLAYER);
    }

    public PetrificationEffect() {
        super(MobEffectCategory.HARMFUL, 0x808080);
    }

    public boolean shouldTransform(LivingEntity entity) {
        return !BLACKLISTED_ENTITIES.contains(entity.getType());
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // ...
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!entity.level().isClientSide() && shouldTransform(entity)) {
            StatueBlock.replaceEntityWithStatue(entity);
        }
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}