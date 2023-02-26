package com.infamous.all_bark_all_bite.common.entity;

import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface OwnableMob extends OwnableEntity {

    String OWNER_TAG = "Owner";

    default LivingEntity cast(){
        return (LivingEntity) this;
    }

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

    default void setOwner(LivingEntity owner) {
        this.setOwnerUUID(owner.getUUID());
        this.setOwnerId(owner.getId());
        this.setCachedOwner(owner);
    }

    default boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    int getOwnerId();

    void setOwnerId(int ownerId);

    @Nullable
    @Override
    default LivingEntity getOwner() {
        UUID ownerUUID = this.getOwnerUUID();
        if(ownerUUID == null) return null;

        LivingEntity cachedOwner = this.getCachedOwner();
        int ownerId = this.getOwnerId();
        // return cached owner if not null, not removed from level and its id matches the synced owner id
        if (cachedOwner != null && !cachedOwner.isRemoved() && cachedOwner.getId() == ownerId) {
            return cachedOwner;
        }
        // if on server, find and cache owner from owner UUID, and sync its network id to the client
        else if (this.cast().level instanceof ServerLevel serverLevel) {
            LivingEntity owner = AiUtil.getLivingEntityFromUUID(serverLevel, ownerUUID).orElse(null);
            this.setCachedOwner(owner);
            this.setOwnerId(owner != null ? owner.getId() : 0);
            return owner;
        }
        // if on client, find and cache owner from synced owner id
        else if(ownerId != 0) {
            LivingEntity owner = AiUtil.getLivingEntityFromId(this.cast().level, ownerId).orElse(null);
            this.setCachedOwner(owner);
            return owner;
        } else {
            return null;
        }
    }

    void setCachedOwner(@Nullable LivingEntity cachedOwner);

    @Nullable LivingEntity getCachedOwner();
}
