package dev.kn0wng1itch.losttoys.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.kn0wng1itch.losttoys.block.entity.StatueBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatueBlockRenderer implements BlockEntityRenderer<StatueBlockEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatueBlockRenderer.class);
    private static final boolean DEBUG_MODE = true;
    private static final int RENDER_DISTANCE_CHUNKS = 4;
    private static final int RENDER_DISTANCE_BLOCKS = RENDER_DISTANCE_CHUNKS * 16;

    public StatueBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StatueBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // Check if player is within render distance
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // Calculate squared distance to player
        double distanceSquared = minecraft.player.distanceToSqr(
                entity.getBlockPos().getX() + 0.5,
                entity.getBlockPos().getY() + 0.5,
                entity.getBlockPos().getZ() + 0.5
        );

        // Skip rendering if too far away
        if (distanceSquared > RENDER_DISTANCE_BLOCKS * RENDER_DISTANCE_BLOCKS) return;

        if (entity.getStoredEntityData() == null || entity.getStoredEntityType() == null) {
            return;
        }

        if (entity.getLevel() == null) {
            return;
        }

        Entity dummyEntity = entity.getCachedEntity();
        if (dummyEntity == null) {
            return;
        }

        poseStack.pushPose();
        try {
            // Position the entity correctly
            poseStack.translate(0.5, 0, 0.5);
            poseStack.translate(0, entity.getYOffset(), 0);

            // Stop all mob AI and brain activities
            if (dummyEntity instanceof Mob mob) {
                mob.getBrain().removeAllBehaviors();
                mob.setNoAi(true);
            }

            // Set entity rotation, including both current and old values to prevent interpolation
            float yaw = entity.getEntityYaw();
            float pitch = entity.getEntityPitch();

            dummyEntity.setYRot(yaw);
            dummyEntity.setXRot(pitch);
            dummyEntity.yRotO = yaw;
            dummyEntity.xRotO = pitch;

            // Disable ticking
            dummyEntity.tickCount = 0;

            // Render the entity with fixed lighting
            EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderer<? super Entity> entityRenderer = renderDispatcher.getRenderer(dummyEntity);

            // Use a fixed light value for better visibility
            entityRenderer.render(dummyEntity, 0, partialTick, poseStack, bufferSource, 15728880);

            if (DEBUG_MODE) {
                LOGGER.info("Rendering statue entity: {}, Yaw: {}, Pitch: {}",
                        dummyEntity.getType(), yaw, pitch);
            }
        } catch (Exception e) {
            LOGGER.error("StatueBlockRenderer: Error during rendering: {}", e.getMessage(), e);
        } finally {
            poseStack.popPose();
        }
    }
}