package dev.kn0wng1itch.losttoys.block.entity;

import dev.kn0wng1itch.losttoys.LostToys;
import dev.kn0wng1itch.losttoys.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StatueBlockEntity extends BlockEntity {
    private CompoundTag storedEntityData;
    private EntityType<?> storedEntityType;
    private UUID entityUUID;
    private int lightExposureTime = 0;
    private static final int REQUIRED_LIGHT_TICKS = 20;
    private float entityYaw;
    private float entityPitch;
    private float entityHeadYaw;
    private float entityHeadYawO;
    private float entityBodyYaw;
    private float entityBodyYawO;
    private transient Entity cachedEntity;
    private static final boolean DEBUG = true;

    public StatueBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STATUE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
        if (DEBUG) {
            System.out.println("StatueBlockEntity: Received data packet from server with data: " + tag);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
        if (DEBUG) {
            System.out.println("StatueBlockEntity: Client handling update tag with data: " + tag);
        }
    }

    public void setStoredEntity(Entity entity) {
        this.storedEntityType = entity.getType();
        this.entityUUID = entity.getUUID();
        this.storedEntityData = new CompoundTag();

        // Store both current and previous rotation values to prevent interpolation
        this.entityYaw = entity.getYRot();
        this.entityPitch = entity.getXRot();

        // If it's a living entity, store additional rotation data
        if (entity instanceof LivingEntity livingEntity) {
            CompoundTag extraData = new CompoundTag();
            extraData.putFloat("YBodyRot", livingEntity.yBodyRot);

            // Add animation state data
            extraData.putInt("Age", livingEntity.tickCount);
            extraData.putInt("SwingTime", livingEntity.swingTime);
            extraData.putBoolean("Swinging", livingEntity.swinging);

            // For mobs, save additional data
            if (livingEntity instanceof Mob mob) {
                extraData.putFloat("MobYBodyRot", mob.yBodyRot);
            }

            // Capture Head Rotations
            this.entityHeadYaw = livingEntity.yHeadRot;
            this.entityHeadYawO = livingEntity.yHeadRotO;

            // Capture Body Rotations
            this.entityBodyYaw = livingEntity.yBodyRot;
            this.entityBodyYawO = livingEntity.yBodyRotO;

            // Store the extra data in the entity's saved data
            entity.saveWithoutId(storedEntityData);
            storedEntityData.put("StatueExtraData", extraData);
        } else {
            entity.saveWithoutId(storedEntityData);
        }

        if (DEBUG) {
            System.out.println("StatueBlockEntity: Stored Entity Type: " + entity.getType());
            System.out.println("StatueBlockEntity: Stored Entity Data: " + storedEntityData);
            System.out.println("StatueBlockEntity: Entity UUID: " + entityUUID);
            System.out.println("StatueBlockEntity: Stored Entity Yaw: " + entityYaw);
            System.out.println("StatueBlockEntity: Stored Entity Pitch: " + entityPitch);
        }

        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        cachedEntity = null; // Invalidate cache
    }

    public AABB getEntityShape() {
        if (storedEntityType != null) {
            assert level != null;
            Entity dummyEntity = storedEntityType.create(level);
            if (dummyEntity != null) {
                return dummyEntity.getBoundingBox();
            }
        }
        return new AABB(worldPosition);
    }

    public void restoreEntity() {
        if (level instanceof ServerLevel serverLevel && storedEntityType != null && storedEntityData != null) {
            Entity entity = storedEntityType.create(serverLevel);
            if (entity != null) {
                try {
                    // Remove StatueExtraData before loading the entity
                    CompoundTag dataToLoad = storedEntityData.copy();
                    dataToLoad.remove("StatueExtraData");

                    entity.load(dataToLoad);
                    entity.moveTo(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5, entityYaw, entityPitch);

                    // If it's a mob, reset its AI state
                    if (entity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }

                    serverLevel.addFreshEntity(entity);
                    serverLevel.removeBlock(worldPosition, false);
                } catch (Exception e) {
                    LostToys.LOGGER.error("Failed to restore entity", e);
                }
            } else {
                LostToys.LOGGER.warn("Failed to create stored entity type: {}", storedEntityType);
            }
        }
    }

    private static void sendChat(MinecraftServer server, String message) {
        if (server != null && DEBUG) {
            PlayerList playerList = server.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                player.sendSystemMessage(Component.literal(message));
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StatueBlockEntity statue) {
        if (level.getGameTime() % 10 == 0 && !level.isClientSide) {
            int lightLevel = level.getMaxLocalRawBrightness(pos);

            if (level instanceof ServerLevel serverLevel) {
                if (DEBUG) {
                    sendChat(serverLevel.getServer(), "Statue Pos: " + pos + ", Light Level: " + lightLevel + ", Exposure: " + statue.lightExposureTime);
                }

                if (lightLevel >= 10) {
                    statue.lightExposureTime++;
                    if (statue.lightExposureTime >= REQUIRED_LIGHT_TICKS) {
                        if (DEBUG) {
                            sendChat(serverLevel.getServer(), "Statue restoring at: " + pos);
                        }
                        statue.restoreEntity();
                    }
                } else {
                    if (statue.lightExposureTime > 0) {
                        statue.lightExposureTime--;
                    }
                }

                statue.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("StoredEntityData")) {
            storedEntityData = tag.getCompound("StoredEntityData");
            storedEntityType = EntityType.byString(tag.getString("StoredEntityType")).orElse(null);
            entityUUID = tag.hasUUID("EntityUUID") ? tag.getUUID("EntityUUID") : null;
            entityYaw = tag.getFloat("EntityYaw");
            entityPitch = tag.getFloat("EntityPitch");
            entityHeadYaw = tag.getFloat("EntityHeadYaw");
            entityHeadYawO = tag.getFloat("EntityHeadYawO");
            entityBodyYaw = tag.getFloat("EntityBodyYaw");
            entityBodyYawO = tag.getFloat("EntityBodyYawO");

            if (DEBUG) {
                System.out.println("StatueBlockEntity: Loaded Entity Type: " + storedEntityType);
                System.out.println("StatueBlockEntity: Loaded Entity Data: " + storedEntityData);
                System.out.println("StatueBlockEntity: Loaded Entity UUID: " + entityUUID);
                System.out.println("StatueBlockEntity: Loaded Entity Yaw: " + entityYaw);
                System.out.println("StatueBlockEntity: Loaded Entity Pitch: " + entityPitch);
                System.out.println("StatueBlockEntity: Loaded Entity Head Yaw: " + entityHeadYaw);
                System.out.println("StatueBlockEntity: Loaded Entity Head YawO: " + entityHeadYawO);
                System.out.println("StatueBlockEntity: Loaded Entity Body Yaw: " + entityBodyYaw);
                System.out.println("StatueBlockEntity: Loaded Entity Body YawO: " + entityBodyYawO);
                System.out.println("StatueBlockEntity: Loaded data immediately: " + storedEntityData);
            }
        }
        lightExposureTime = tag.getInt("LightExposureTime");
        if (DEBUG) {
            System.out.println("StatueBlockEntity: Loaded Light Exposure Time: " + lightExposureTime);
        }
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        setChanged();
        cachedEntity = null; // Invalidate cache
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (storedEntityData != null && storedEntityType != null) {
            tag.put("StoredEntityData", storedEntityData);
            tag.putString("StoredEntityType", EntityType.getKey(storedEntityType).toString());
            if (entityUUID != null) {
                tag.putUUID("EntityUUID", entityUUID);
            }
            tag.putFloat("EntityYaw", entityYaw);
            tag.putFloat("EntityPitch", entityPitch);
            tag.putFloat("EntityHeadYaw", entityHeadYaw);
            tag.putFloat("EntityHeadYawO", entityHeadYawO);
            tag.putFloat("EntityBodyYaw", entityBodyYaw);
            tag.putFloat("EntityBodyYawO", entityBodyYawO);

            if (DEBUG) {
                System.out.println("StatueBlockEntity: Saved Entity Type: " + storedEntityType);
                System.out.println("StatueBlockEntity: Saved Entity Data: " + storedEntityData);
                System.out.println("StatueBlockEntity: Saved Entity UUID: " + entityUUID);
                System.out.println("StatueBlockEntity: Saved Entity Yaw: " + entityYaw);
                System.out.println("StatueBlockEntity: Saved Entity Pitch: " + entityPitch);
                System.out.println("StatueBlockEntity: Saved Entity Head Yaw: " + entityHeadYaw);
                System.out.println("StatueBlockEntity: Saved Entity Head YawO: " + entityHeadYawO);
                System.out.println("StatueBlockEntity: Saved Entity Body Yaw: " + entityBodyYaw);
                System.out.println("StatueBlockEntity: Saved Entity Body YawO: " + entityBodyYawO);
            }
        }
        tag.putInt("LightExposureTime", lightExposureTime);
        if (DEBUG) {
            System.out.println("StatueBlockEntity: Saved Light Exposure Time: " + lightExposureTime);
        }
    }

    public CompoundTag getStoredEntityData() {
        return storedEntityData;
    }

    public EntityType<?> getStoredEntityType() {
        return storedEntityType;
    }

    public double getYOffset() {
        return 0.0;
    }

    public float getEntityYaw() {
        return entityYaw;
    }

    public float getEntityPitch() {
        return entityPitch;
    }

    public Entity getCachedEntity() {
        if (cachedEntity == null && storedEntityType != null && storedEntityData != null && level != null) {
            cachedEntity = storedEntityType.create(level);
            if (cachedEntity != null) {
                try {
                    // Create a copy of the data to avoid modifying the original
                    CompoundTag dataToLoad = storedEntityData.copy();

                    // Load the basic entity data
                    cachedEntity.load(dataToLoad);

                    // Set rotation values for all entity types
                    cachedEntity.setYRot(entityYaw);
                    cachedEntity.setXRot(entityPitch);
                    cachedEntity.yRotO = entityYaw; // Previous rotation
                    cachedEntity.xRotO = entityPitch; // Previous rotation

                    // Additional settings for living entities
                    if (cachedEntity instanceof LivingEntity livingEntity) {
                        // Get extra data if available
                        if (storedEntityData.contains("StatueExtraData")) {
                            CompoundTag extraData = storedEntityData.getCompound("StatueExtraData");

                            // Set body rotation
                            float bodyRot = extraData.contains("YBodyRot") ? extraData.getFloat("YBodyRot") : entityYaw;

                            livingEntity.yBodyRot = bodyRot;
                            livingEntity.yBodyRotO = bodyRot;

                            // Set head rotation
                            livingEntity.yHeadRot = entityHeadYaw;
                            livingEntity.yHeadRotO = entityHeadYawO;

                            // Set body rotation
                            livingEntity.yBodyRot = entityBodyYaw;
                            livingEntity.yBodyRotO = entityBodyYawO;

                            // Restore animation state
                            if (extraData.contains("Age"))
                                livingEntity.tickCount = extraData.getInt("Age");
                            if (extraData.contains("SwingTime"))
                                livingEntity.swingTime = extraData.getInt("SwingTime");
                            if (extraData.contains("Swinging"))
                                livingEntity.swinging = extraData.getBoolean("Swinging");

                            // For mobs, restore additional data
                            if (livingEntity instanceof Mob mob && extraData.contains("MobYBodyRot"))
                                mob.yBodyRot = extraData.getFloat("MobYBodyRot");
                        } else {
                            // If no extra data, use the entity yaw for all rotations
                            livingEntity.yBodyRot = entityYaw;
                            livingEntity.yBodyRotO = entityYaw;
                            livingEntity.yHeadRot = entityYaw;
                            livingEntity.yHeadRotO = entityYaw;
                        }

                        // Reset hurt animation state
                        livingEntity.hurtTime = 0;
                        livingEntity.hurtDuration = 0;
                        livingEntity.deathTime = 0;
                    }

                    // Disable AI for mobs
                    if (cachedEntity instanceof Mob mob) {
                        mob.setNoAi(true);
                    }

                    // Disable ticking
                    cachedEntity.tickCount = 0;

                } catch (Exception e) {
                    LostToys.LOGGER.error("Failed to create cached entity", e);
                    return null;
                }
            }
        }
        return cachedEntity;
    }
}