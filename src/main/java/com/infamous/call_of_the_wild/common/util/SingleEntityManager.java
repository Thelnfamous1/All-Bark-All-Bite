package com.infamous.call_of_the_wild.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SingleEntityManager {
    private static final int CONVERSION_DELAY = 2;
    private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 2);
    @Nullable
    private Entity entity;
    @Nullable
    private UUID entityUuid;

    public static Codec<SingleEntityManager> codec() {
        return RecordCodecBuilder
                .create((instance) -> instance.group(
                                ExtraCodecs.UUID
                                        .optionalFieldOf("uuid")
                                        .forGetter(SingleEntityManager::getEntityUUID))
                        .apply(instance, uuid -> new SingleEntityManager(uuid.orElse(null))));
    }

    public SingleEntityManager() {}

    public SingleEntityManager(@Nullable UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    public Optional<UUID> getEntityUUID() {
        return Optional.ofNullable(this.entity != null ? this.entity.getUUID() : this.entityUuid);
    }

    public void tick(ServerLevel level, Predicate<Entity> isValid, Consumer<Entity> onInvalid) {
        --this.conversionDelay;
        if (this.conversionDelay <= 0) {
            this.convertFromUuid(level);
            this.conversionDelay = CONVERSION_DELAY;
        }

        if(this.entity != null){
            Entity.RemovalReason removalReason = this.entity.getRemovalReason();
            boolean valid = isValid.test(this.entity);
            boolean isRemoved = removalReason != null;
            if(!valid || isRemoved){
                if(!valid) onInvalid.accept(this.entity);
                if (isRemoved) {
                    switch (removalReason) {
                        case CHANGED_DIMENSION, UNLOADED_TO_CHUNK, UNLOADED_WITH_PLAYER ->
                                this.entityUuid = this.entity.getUUID();
                    }
                }
                this.entity = null;
            }
        }
    }

    private void convertFromUuid(ServerLevel level) {
        if(this.entityUuid != null){
            Entity entity = level.getEntity(this.entityUuid);
            if(entity != null){
                this.entity = entity;
                this.entityUuid = null;
            }
        }
    }

    public boolean hasEntity(){
        return this.entity != null || this.entityUuid != null;
    }

    public Optional<Entity> getEntity(){
        return Optional.ofNullable(this.entity);
    }

    public void set(Entity entity) {
        this.entity = entity;
        this.entityUuid = null;
    }

    public void erase() {
        this.entity = null;
        this.entityUuid = null;
    }
}
