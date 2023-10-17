package com.infamous.all_bark_all_bite.common.entity.illager_hound;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.entity.EntityAnimationController;
import com.infamous.all_bark_all_bite.common.entity.OwnableMob;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.util.DebugUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class IllagerHound extends Monster implements OwnableMob {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(IllagerHound.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_OWNERID_ID = SynchedEntityData.defineId(IllagerHound.class, EntityDataSerializers.INT);

    public final EntityAnimationController<IllagerHound> animationController;
    @Nullable
    private LivingEntity cachedOwner;

    public IllagerHound(EntityType<? extends IllagerHound> entityType, Level level) {
        super(entityType, level);
        this.xpReward = Enemy.XP_REWARD_MEDIUM;
        this.animationController = new EntityAnimationController<>(this, Entity.DATA_POSE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        this.entityData.define(DATA_OWNERID_ID, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.writeOwnerNBT(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readOwnerNBT(tag);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isOwnedBy(target) && super.canAttack(target);
    }

    @Override
    public Team getTeam() {
        return this.getOwnerTeam().orElse(super.getTeam());
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return this.isOwnerAlliedTo(other) || super.isAlliedTo(other);
    }

    @Override
    protected Brain.Provider<IllagerHound> brainProvider() {
        return Brain.provider(IllagerHoundAi.MEMORY_TYPES, IllagerHoundAi.SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<IllagerHound> brain = this.brainProvider().makeBrain(dynamic);
        IllagerHoundAi.makeBrain(brain);
        return brain;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<IllagerHound> getBrain() {
        return (Brain<IllagerHound>) super.getBrain();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if(this.animationController != null){
            this.animationController.onSyncedDataUpdatedAnimations(entityDataAccessor);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.animationController.tickAnimations();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.animationController.aiStepAnimations();
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, EntityAnimationController.JUMPING_EVENT_ID);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            amount = SharedWolfAi.maybeReduceDamage(amount, source);
            return super.hurt(source, amount);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.level().broadcastEntityEvent(this, EntityAnimationController.ATTACKING_EVENT_ID);
        return super.doHurtTarget(target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        this.animationController.handleEntityEventAnimation(id);
        super.handleEntityEvent(id);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("houndBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("houndActivityUpdate");
        IllagerHoundAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
        if(AllBarkAllBite.ENABLE_BRAIN_DEBUG && this.level() instanceof ServerLevel serverLevel){
            DebugUtil.sendEntityBrain(this, serverLevel, MemoryModuleType.DUMMY);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().isClientSide) {
            return null;
        } else {
            return this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? SoundEvents.WOLF_GROWL : SoundEvents.WOLF_AMBIENT;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return !this.isAggressive() && !this.isLeashed();
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.8F;
    }

    @Override
    public float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0D, 0.6F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public ItemStack getPickResult() {
        return ForgeSpawnEggItem.fromEntityType(this.getType()).getDefaultInstance();
    }

    protected void playAngrySound() {
        this.playSound(SoundEvents.WOLF_GROWL, this.getSoundVolume(), this.getVoicePitch());
    }

    // OwnableMob

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    @Override
    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(ownerUUID));
    }

    @Override
    public int getOwnerId() {
        return this.entityData.get(DATA_OWNERID_ID);
    }

    @Override
    public void setOwnerId(int ownerId){
        this.entityData.set(DATA_OWNERID_ID, ownerId);
    }

    @Override
    public void setCachedOwner(@Nullable LivingEntity cachedOwner) {
        this.cachedOwner = cachedOwner;
    }

    @Override
    @Nullable
    public LivingEntity getCachedOwner() {
        return this.cachedOwner;
    }
}
