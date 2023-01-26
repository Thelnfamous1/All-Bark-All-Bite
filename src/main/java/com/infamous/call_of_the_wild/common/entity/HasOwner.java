package com.infamous.call_of_the_wild.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface HasOwner extends OwnableEntity {

    String OWNER_TAG = "Owner";

    default void writeOwnerNBT(CompoundTag tag) {
        if (this.getOwnerUUID() != null) {
            tag.putUUID(OWNER_TAG, this.getOwnerUUID());
        }
    }

    default void readOwnerNBT(CompoundTag tag) {
        if (tag.hasUUID(OWNER_TAG)) {
            UUID uuid = tag.getUUID(OWNER_TAG);
            this.setOwnerUUID(uuid);
        }
    }

    default Optional<Team> getOwnerTeam() {
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            return Optional.ofNullable(owner.getTeam());
        }
        return Optional.empty();
    }

    default boolean isOwnerAlliedTo(Entity other) {
        LivingEntity owner = this.getOwner();
        if (other == owner) {
            return true;
        }

        if (owner != null) {
            return owner.isAlliedTo(other);
        }
        return false;
    }

    void setOwnerUUID(@Nullable UUID ownerUUID);

    void setOwner(LivingEntity owner);

    default boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    @Nullable
    @Override
    LivingEntity getOwner();
}
