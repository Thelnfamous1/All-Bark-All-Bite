package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.entity.*;
import com.infamous.call_of_the_wild.common.registry.*;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.MiscUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@SuppressWarnings("NullableProblems")
public class Dog extends TamableAnimal implements InterestedMob, ShakingMob, VariantMob, CollaredMob {
    public static final Collection<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.DIG_COOLDOWN,
            COTWMemoryModuleTypes.DIG_LOCATION.get(),
            COTWMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get(),
            COTWMemoryModuleTypes.DISABLE_WALK_TO_PLAY_ITEM.get(),
            COTWMemoryModuleTypes.FETCHING_DISABLED.get(),
            COTWMemoryModuleTypes.FETCHING_ITEM.get(),
            //MemoryModuleType.HAS_HUNTING_COOLDOWN,
            MemoryModuleType.HUNTED_RECENTLY,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
            MemoryModuleType.LOOK_TARGET,
            COTWMemoryModuleTypes.NEAREST_ADULTS.get(),
            COTWMemoryModuleTypes.NEAREST_BABIES.get(),
            COTWMemoryModuleTypes.NEAREST_ALLIES.get(),
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
            COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
            COTWMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(),
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            MemoryModuleType.PATH,
            COTWMemoryModuleTypes.PLAYING_DISABLED.get(),
            COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(),
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get(),
            COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_PLAY_ITEM.get(),
            MemoryModuleType.UNIVERSAL_ANGER,
            MemoryModuleType.WALK_TARGET
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Dog>>> SENSOR_TYPES = ImmutableList.of(
            COTWSensorTypes.ANIMAL_TEMPTATIONS.get(),
            SensorType.HURT_BY,

            // dependent on NEAREST_VISIBLE_LIVING_ENTITIES
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_ADULT,
            COTWSensorTypes.NEAREST_KIN.get(),
            COTWSensorTypes.DOG_SPECIFIC_SENSOR.get(),

            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_PLAYERS
    );
    @SuppressWarnings("unused")
    private static final int FLAG_SITTING = 1; // Used by TamableAnimal
    @SuppressWarnings("unused")
    private static final int FLAG_TAME = 4; // Used by TamableAnimal
    private static final int FLAG_INTERESTED = 8;
    private static final int FLAG_WET = 16;
    private static final int FLAG_SHAKING = 32;
    private static final EntityDataAccessor<EntityVariant> DATA_VARIANT_ID = SynchedEntityData.defineId(Dog.class, COTWEntityDataSerializers.DOG_VARIANT.get());
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Dog.class, EntityDataSerializers.INT);
    private static final byte JUMPING_ID = (byte) 1;
    private final MutablePair<Float, Float> interestedAngles = new MutablePair<>(0.0F, 0.0F);

    public final AnimationState babyAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState shakeAnimationState = new AnimationState();
    public final AnimationState diggingAnimationState = new AnimationState();
    private int jumpTicks;
    private int jumpDuration;
    private final MutablePair<Float, Float> shakeAnims = new MutablePair<>(0.0F, 0.0F);
    private static final double START_HEALTH = 8.0D;
    private static final double TAME_HEALTH = 20.0D;

    public Dog(EntityType<? extends Dog> type, Level level) {
        super(type, level);
        this.setTame(false);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
        this.setCanPickUpLoot(this.canPickUpLoot());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.MAX_HEALTH, START_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT_ID, COTWDogVariants.BROWN.get());
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if(DATA_FLAGS_ID.equals(dataAccessor)){
            if(this.level.isClientSide){
                if(this.isInSittingPose()){
                    this.idleAnimationState.stop();
                    this.walkAnimationState.stop();
                    this.runAnimationState.stop();
                    this.jumpAnimationState.stop();
                    this.sitAnimationState.startIfStopped(this.tickCount);
                } else{
                    this.sitAnimationState.stop();
                }
            }
        } else if(DATA_POSE.equals(dataAccessor)){
            if (this.getPose() == Pose.DIGGING) {
                this.diggingAnimationState.start(this.tickCount);
            }
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
    protected float getSoundVolume() {
        return 0.4F;
    }

    protected void playSoundEvent(SoundEvent soundEvent) {
        this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.aiStepShaking();
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
        }
    }

    @Override
    public void tick() {
        if (this.level.isClientSide()) {
            if(this.isBaby()){
                this.babyAnimationState.startIfStopped(this.tickCount);
            } else{
                this.babyAnimationState.stop();
            }

            boolean midJump = this.jumpDuration != 0;
            if(!midJump && this.jumpAnimationState.isStarted()) this.jumpAnimationState.stop();
            boolean moving = this.isMovingOnLandOrInWater();

            if(!this.isInSittingPose()){
                if (moving && !midJump) {
                    this.idleAnimationState.stop();
                    if(this.isSprinting()){
                        this.walkAnimationState.stop();
                        this.runAnimationState.startIfStopped(this.tickCount);
                    } else{
                        this.runAnimationState.stop();
                        this.walkAnimationState.startIfStopped(this.tickCount);
                    }
                } else {
                    if(!midJump) this.idleAnimationState.startIfStopped(this.tickCount);
                    this.walkAnimationState.stop();
                    this.runAnimationState.stop();
                }
            }

            if (this.getPose() == Pose.DIGGING) {
                this.clientDiggingParticles(this.diggingAnimationState);
            }
        }

        super.tick();
        if(this.isAlive()){
            this.tickInterest();
            this.tickShaking();
        }
    }

    private boolean isMovingOnLandOrInWater() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D && (this.isOnGround() || this.isInWaterOrBubble());
    }

    private void clientDiggingParticles(AnimationState animationState) {
        if ((float)animationState.getAccumulatedTime() < 4500.0F) {
            RandomSource random = this.getRandom();
            BlockState blockStateOn = this.getBlockStateOn();
            if (blockStateOn.getRenderShape() != RenderShape.INVISIBLE) {
                for(int particleCount = 0; particleCount < 10; ++particleCount) {
                    double x = this.getX() + (double) Mth.randomBetween(random, -0.7F, 0.7F);
                    double y = this.getY();
                    double z = this.getZ() + (double)Mth.randomBetween(random, -0.7F, 0.7F);
                    this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockStateOn), x, y, z, 0.0D, 0.0D, 0.0D);
                }
            }
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
            if (!this.level.isClientSide) {
                this.setOrderedToSit(false);
            }

            // for some reason, vanilla Wolves take reduced damage from non-players and non-arrows
            if (sourceEntity != null && !(sourceEntity instanceof Player) && !(sourceEntity instanceof AbstractArrow)) {
                amount = (amount + 1.0F) / 2.0F;
            }

            return super.hurt(source, amount);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void setTame(boolean tame) {
        super.setTame(tame);
        if (tame) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(TAME_HEALTH);
            this.setHealth((float)TAME_HEALTH);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(START_HEALTH);
        }

        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
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
            return DogAi.mobInteract(this, player, hand, () -> super.mobInteract(player, hand));
        }
    }

    @Override
    protected void usePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        if (this.isFood(stack) && !this.level.isClientSide) {
            this.playSoundEvent(this.getEatingSound(stack));

            float healAmount = 1.0F;
            FoodProperties foodProperties = stack.getFoodProperties(this);
            if(foodProperties != null){
                healAmount = foodProperties.getNutrition();
                AiUtil.addEatEffect(this, level, foodProperties);
            }
            if(this.isInjured()) this.heal(healAmount);

            this.gameEvent(GameEvent.EAT, this);
        }

        super.usePlayerItem(player, hand, stack);
    }

    public boolean isInjured(){
        return this.getHealth() < this.getMaxHealth();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if(id == JUMPING_ID){
            this.jumpAnimationState.startIfStopped(this.tickCount);
            this.jumpDuration = 10; // half a second, which is the same length as the jump animation
            this.jumpTicks = 0;
        }
        else if(!this.handleShakingEvent(id)) super.handleEntityEvent(id);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        FoodProperties foodProperties = stack.getFoodProperties(this);
        return stack.is(COTWTags.DOG_FOOD) || foodProperties != null && foodProperties.isMeat();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Dog offspring = COTWEntityTypes.DOG.get().create(level);
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
            } else if (this.isInSittingPose() || mate.isInSittingPose()) {
                return false;
            } else {
                return this.isInLove() && mate.isInLove();
            }
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof Creeper) && !(target instanceof Ghast)) {
            if (target instanceof Dog dog) {
                return !dog.isTame() || dog.getOwner() != owner;
            } else if (target instanceof Wolf wolf) {
                return !wolf.isTame() || wolf.getOwner() != owner;
            } else if (target instanceof Player targetPlayer && owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(targetPlayer)) {
                return false;
            } else if (target instanceof AbstractHorse horse && horse.isTamed()) {
                return false;
            } else {
                return !(target instanceof TamableAnimal tamable) || !tamable.isTame();
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
        SharedWolfAi.initMemories(this, random);
        return spawnGroupData;
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, JUMPING_ID);
        }
    }

    @Override
    protected Brain.Provider<Dog> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return DogAi.makeBrain(this.brainProvider().makeBrain(dynamic));
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
        this.level.getProfiler().push("dogActivityUpdate");
        DogAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return ForgeEventFactory.getMobGriefingEvent(this.level, this) && this.canPickUpLoot() && DogAi.wantsToPickup(this, stack);
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        this.onItemPickup(itemEntity);
        DogAi.pickUpItem(this, itemEntity);
    }

    @Override
    public boolean canPickUpLoot() {
        return !this.isOnPickupCooldown();
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
    public SoundEvent getEatingSound(ItemStack stack) {
        return SoundEvents.FOX_EAT;
    }

    protected void holdInMouth(ItemStack stack) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, stack);
    }

    protected boolean hasItemInMouth() {
        return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
    }

    public ItemStack getItemInMouth(){
        return this.getItemInHand(InteractionHand.MAIN_HAND);
    }

    @SuppressWarnings("SameParameterValue")
    protected void setItemInMouth(ItemStack stack){
        this.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    protected boolean isOnPickupCooldown() {
        return this.getBrain().hasMemoryValue(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
        /*
        if(this.level instanceof  ServerLevel serverLevel){
            DebugUtil.sendEntityBrain(this, serverLevel);
        }
         */
    }

    // VariantMob

    @Override
    public IForgeRegistry<EntityVariant> getVariantRegistry() {
        return COTWDogVariants.DOG_VARIANT_REGISTRY.get();
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

    @Override
    public AnimationState getShakeAnimationState() {
        return this.shakeAnimationState;
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
