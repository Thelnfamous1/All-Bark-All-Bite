package com.infamous.all_bark_all_bite.common.entity.dog;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.compat.CompatUtil;
import com.infamous.all_bark_all_bite.common.compat.RWCompat;
import com.infamous.all_bark_all_bite.common.entity.*;
import com.infamous.all_bark_all_bite.common.registry.ABABDogVariants;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityDataSerializers;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.DebugUtil;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ai.DigAi;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
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
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class Dog extends Wolf implements VariantMob{
    private static final EntityDataAccessor<EntityVariant> DATA_VARIANT_ID = SynchedEntityData.defineId(Dog.class, ABABEntityDataSerializers.DOG_VARIANT.get());

    public Dog(EntityType<? extends Dog> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(true);
        this.getNavigation().setCanFloat(true);
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT_ID, ABABDogVariants.BROWN.get());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addVariantSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readVariantSaveData(tag);
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
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(CompatUtil.isRWLoaded()){
            Optional<InteractionResult> rwResult = RWCompat.mobInteract(this, player, hand);
            if(rwResult.isPresent()) return rwResult.get();
        }

        ItemStack stack = player.getItemInHand(hand);
        if (this.level.isClientSide) {
            boolean canInteract = this.isOwnedBy(player)
                    || this.isTame()
                    || this.isFood(stack) && !this.isTame() && !this.isAggressive();
            return canInteract ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            Optional<InteractionResult> mobInteract = DogAi.mobInteract(this, player, hand);
            if(mobInteract.isEmpty()){
                InteractionResult animalInteractResult = ((AnimalAccess)this).animalInteract(player, hand);
                if(animalInteractResult.consumesAction()){
                    this.setPersistenceRequired();
                }
                boolean willNotBreed = !animalInteractResult.consumesAction();
                if (willNotBreed && this.isOwnedBy(player)) {
                    SharedWolfAi.manualCommand(this, player);
                    DigAi.eraseDigLocation(this); // prevents bug where dogs that get up from sitting still try to dig after dropping the given item
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
    public boolean isFood(ItemStack stack) {
        FoodProperties foodProperties = stack.getFoodProperties(this);
        return stack.is(ABABTags.DOG_FOOD) || foodProperties != null && foodProperties.isMeat();
    }

    @Nullable
    @Override
    public Dog getBreedOffspring(ServerLevel level, AgeableMob partner) {
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
    public boolean removeWhenFarAway(double distFromNearestPlayer) {
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
}
