package com.infamous.all_bark_all_bite.common.entity.dog;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.entity.*;
import com.infamous.all_bark_all_bite.common.registry.ABABDogVariants;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityDataSerializers;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.DebugUtil;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import com.infamous.all_bark_all_bite.config.ABABConfig;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class Dog extends TamableAnimal implements InterestedMob, ShakingMob, VariantMob, CollaredMob {
    @SuppressWarnings("unused")
    private static final int FLAG_SITTING = 1; // Used by TamableAnimal
    @SuppressWarnings("unused")
    private static final int FLAG_TAME = 4; // Used by TamableAnimal
    private static final int FLAG_INTERESTED = 8;
    private static final int FLAG_WET = 16;
    private static final int FLAG_SHAKING = 32;
    private static final EntityDataAccessor<EntityVariant> DATA_VARIANT_ID = SynchedEntityData.defineId(Dog.class, ABABEntityDataSerializers.DOG_VARIANT.get());
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Dog.class, EntityDataSerializers.INT);
    private final MutablePair<Float, Float> interestedAngles = new MutablePair<>(0.0F, 0.0F);

    public final SharedWolfAnimationController animationController;
    private final MutablePair<Float, Float> shakeAnims = new MutablePair<>(0.0F, 0.0F);

    public Dog(EntityType<? extends Dog> type, Level level) {
        super(type, level);
        this.setTame(false);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
        this.setCanPickUpLoot(this.canPickUpLoot());
        this.getNavigation().setCanFloat(true);
        this.animationController = new SharedWolfAnimationController(this, TamableAnimal.DATA_FLAGS_ID, Entity.DATA_POSE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.MAX_HEALTH, ABABConfig.dogMaxHealth.get())
                .add(Attributes.ATTACK_DAMAGE, ABABConfig.dogAttackDamage.get());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT_ID, ABABDogVariants.BROWN.get());
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if(this.animationController != null){
            this.animationController.onSyncedDataUpdatedAnimations(dataAccessor);
        }
    }

    protected boolean getFlag(int flagId) {
        return (this.entityData.get(DATA_FLAGS_ID) & flagId) != 0;
    }

    protected void setFlag(int flagId, boolean flag) {
        if (flag) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | flagId));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~flagId));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addVariantSaveData(tag);
        this.addCollarColorSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readVariantSaveData(tag);
        this.readCollarColorSaveData(tag);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.level.isClientSide ? null : DogAi.getSoundForCurrentActivity(this).orElse(null);
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
    public float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.aiStepShaking();
        this.animationController.aiStepAnimations();
    }

    @Override
    public void tick() {
        if (this.level.isClientSide()) {
            this.animationController.tickAnimations();
        }

        super.tick();
        if(this.isAlive()){
            this.tickInterest();
            this.tickShaking();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.dieShaking();
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.8F;
    }

    @Override
    public int getMaxHeadXRot() {
        return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            Entity sourceEntity = source.getEntity();

            // for some reason, vanilla Wolves take reduced damage from non-players and non-arrows
            if (sourceEntity != null && !(sourceEntity instanceof Player) && !(sourceEntity instanceof AbstractArrow)) {
                amount = (amount + 1.0F) / 2.0F;
            }

            return super.hurt(source, amount);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.level.isClientSide) {
            boolean canInteract = this.isOwnedBy(player)
                    || this.isTame()
                    || this.isFood(stack) && !this.isTame() && !this.isAggressive();
            return canInteract && !this.hasPose(Pose.DIGGING) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            Optional<InteractionResult> mobInteract = DogAi.mobInteract(this, player, hand);
            if(mobInteract.isEmpty()){
                InteractionResult animalInteractResult = super.mobInteract(player, hand);
                if(animalInteractResult.consumesAction()){
                    this.setPersistenceRequired();
                }
                boolean willNotBreed = !animalInteractResult.consumesAction();
                if (willNotBreed && this.isOwnedBy(player)) {
                    SharedWolfAi.manualCommand(this, player);
                    return InteractionResult.CONSUME;
                }
                return animalInteractResult;
            } else{
                return mobInteract.get();
            }
        }
    }

    @Override
    protected void usePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        AiUtil.animalEat(this, stack);
        super.usePlayerItem(player, hand, stack);
    }

    @Override
    public void handleEntityEvent(byte id) {
        this.animationController.handleEntityEventAnimation(id);
        this.handleShakingEvent(id);
        super.handleEntityEvent(id);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        FoodProperties foodProperties = stack.getFoodProperties(this);
        return stack.is(ABABTags.DOG_FOOD) || foodProperties != null && foodProperties.isMeat();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Dog offspring = ABABEntityTypes.DOG.get().create(level);
        if (partner instanceof Dog mate && offspring != null) {
            if (this.random.nextBoolean()) {
                offspring.setVariant(this.getVariant());
            } else {
                offspring.setVariant(mate.getVariant());
            }

            if (this.isTame()) {
                offspring.setOwnerUUID(this.getOwnerUUID());
                offspring.setTame(true);
                if (this.random.nextBoolean()) {
                    offspring.setCollarColor(this.getCollarColor());
                } else {
                    offspring.setCollarColor(mate.getCollarColor());
                }
            }
        }

        return offspring;
    }

    @Override
    public boolean canMate(Animal partner) {
        if (partner == this) {
            return false;
        } else if (!(partner instanceof Dog mate)) {
            return false;
        } else {
            if (this.isTame() != mate.isTame()) {
                return false;
            } else if (!SharedWolfAi.canMove(this) || !SharedWolfAi.canMove(mate)) {
                return false;
            } else {
                return this.isInLove() && mate.isInLove();
            }
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof Creeper) && !(target instanceof Ghast)) {
            if (target instanceof TamableAnimal tamableAnimal) {
                return !tamableAnimal.isTame() || tamableAnimal.getOwner() != owner;
            } else {
                if (target instanceof Player targetPlayer && owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(targetPlayer)) {
                    return false;
                } else return !(target instanceof AbstractHorse horse) || !horse.isTamed();
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return !this.isAggressive() && super.canBeLeashed(player);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0D, 0.6F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, groupData, tag);
        Collection<EntityVariant> values = this.getVariantRegistry().getValues();
        RandomSource random = serverLevelAccessor.getRandom();
        EntityVariant randomVariant = MiscUtil.getRandomObject(values, random);
        this.setVariant(randomVariant);

        ServerLevel serverlevel = serverLevelAccessor.getLevel();
        if (serverlevel.structureManager().getStructureWithPieceAt(this.blockPosition(), ABABTags.DOGS_SPAWN_AS_BLACK).isValid()) {
            this.setVariant(ABABDogVariants.BLACK.get());
            this.setPersistenceRequired();
        }

        SharedWolfAi.initMemories(this, random);
        return spawnGroupData;
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, EntityAnimationController.JUMPING_EVENT_ID);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.level.broadcastEntityEvent(this, EntityAnimationController.ATTACKING_EVENT_ID);
        return super.doHurtTarget(target);
    }

    @Override
    protected Brain.Provider<Dog> brainProvider() {
        return Brain.provider(DogAi.MEMORY_TYPES, DogAi.SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return DogBrain.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<Dog> getBrain() {
        return (Brain<Dog>)super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("dogBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return ForgeEventFactory.getMobGriefingEvent(this.level, this) && this.canPickUpLoot() && DogAi.wantsToPickup(this, stack);
    }

    @Override
    public boolean canPickUpLoot() {
        return !GenericAi.isOnPickupCooldown(this);
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(stack);
        if (!this.getItemBySlot(slot).isEmpty()) {
            return false;
        } else {
            return slot == EquipmentSlot.MAINHAND && super.canTakeItem(stack);
        }
    }

    @Override
    public boolean canHoldItem(ItemStack itemStack) {
        ItemStack itemInMouth = this.getMainHandItem();
        return itemInMouth.isEmpty() || this.isFood(itemStack) && !this.isFood(itemInMouth);
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        this.onItemPickup(itemEntity);
        DogAi.pickUpItem(this, itemEntity);
    }

    @Override
    public SoundEvent getEatingSound(ItemStack stack) {
        return SoundEvents.FOX_EAT;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
        if(AllBarkAllBite.ENABLE_BRAIN_DEBUG && this.level instanceof ServerLevel serverLevel){
            DebugUtil.sendEntityBrain(this, serverLevel,
                    ABABMemoryModuleTypes.FOLLOW_TRIGGER_DISTANCE.get(),
                    ABABMemoryModuleTypes.IS_ALERT.get(),
                    ABABMemoryModuleTypes.IS_FOLLOWING.get(),
                    ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(),
                    ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public ItemStack getPickResult() {
        return ForgeSpawnEggItem.fromEntityType(this.getType()).getDefaultInstance();
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float fallDamageMultiplier) {
        MobEffectInstance jumpEffectInstance = this.getEffect(MobEffects.JUMP);
        float jumpEffectFallReduction = jumpEffectInstance == null ? 0.0F : (float)(jumpEffectInstance.getAmplifier() + 1);
        return Mth.ceil((fallDistance - SharedWolfAi.FALL_REDUCTION - jumpEffectFallReduction) * fallDamageMultiplier);
    }

    @Override
    public boolean removeWhenFarAway(double p_28174_) {
        return !this.isTame() && this.tickCount > 2400;
    }

    // VariantMob

    @Override
    public IForgeRegistry<EntityVariant> getVariantRegistry() {
        return ABABDogVariants.DOG_VARIANT_REGISTRY.get();
    }

    @Override
    public EntityVariant getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    public void setVariant(EntityVariant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant);
    }

    // CollaredMob

    @Override
    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    @Override
    public void setCollarColor(DyeColor collarColor) {
        this.entityData.set(DATA_COLLAR_COLOR, collarColor.getId());
    }

    // ShakingMob

    @Override
    public boolean isWet() {
        return this.getFlag(FLAG_WET);
    }

    @Override
    public void setIsWet(boolean isWet) {
        this.setFlag(FLAG_WET, isWet);
    }

    @Override
    public boolean isShaking() {
        return this.getFlag(FLAG_SHAKING);
    }

    @Override
    public void setIsShaking(boolean isShaking) {
        this.setFlag(FLAG_SHAKING, isShaking);
    }

    @Override
    public MutablePair<Float, Float> getShakeAnims() {
        return this.shakeAnims;
    }

    @Override
    public void playShakeSound() {
        this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    // InterestedMob

    @Override
    public boolean isInterested() {
        return this.getFlag(FLAG_INTERESTED);
    }

    @Override
    public void setIsInterested(boolean isInterested) {
        this.setFlag(FLAG_INTERESTED, isInterested);
    }

    @Override
    public MutablePair<Float, Float> getInterestedAngles() {
        return this.interestedAngles;
    }
}
