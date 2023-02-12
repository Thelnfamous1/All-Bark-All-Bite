package com.infamous.call_of_the_wild.common.ai;

import com.infamous.call_of_the_wild.mixin.LivingEntityAccessor;
import com.infamous.call_of_the_wild.mixin.MobAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.UUID;

public class AiUtil {
    public static final float DEFAULT_FALL_REDUCTION = 3.0F;
    private static final TargetingConditions TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forNonCombat().range(16.0D).ignoreLineOfSight();
    private static final TargetingConditions TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat().range(16.0D).ignoreLineOfSight().ignoreInvisibilityTesting();

    public static final double NEAR_ZERO_DELTA_MOVEMENT = 1.0E-6D;

    public static void addEatEffect(LivingEntity eater, Level level, FoodProperties foodProperties) {
        for(Pair<MobEffectInstance, Float> pair : foodProperties.getEffects()) {
            if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
                eater.addEffect(new MobEffectInstance(pair.getFirst()));
            }
        }
    }

    public static boolean hasAnyMemory(LivingEntity livingEntity, MemoryModuleType<?>... memoryModuleTypes){
        Brain<?> brain = livingEntity.getBrain();
        for(MemoryModuleType<?> memoryModuleType : memoryModuleTypes){
            if(brain.hasMemoryValue(memoryModuleType)) return true;
        }
        return false;
    }

    public static void eraseMemories(LivingEntity livingEntity, MemoryModuleType<?>... memoryModuleTypes) {
        Brain<?> brain = livingEntity.getBrain();
        for (MemoryModuleType<?> memoryModuleType : memoryModuleTypes) {
            brain.eraseMemory(memoryModuleType);
        }
    }

    public static boolean isClose(Mob mob, LivingEntity target, int closeEnough) {
        return target.distanceToSqr(mob) <= Mth.square(closeEnough);
    }

    public static boolean isAttackable(Mob mob, LivingEntity target, boolean requireLineOfSight){
        return requireLineOfSight ? Sensor.isEntityAttackable(mob, target) : Sensor.isEntityAttackableIgnoringLineOfSight(mob, target);
    }

    public static boolean isHuntableBabyTurtle(Mob mob, LivingEntity target, int closeEnough, boolean requireLineOfSight) {
        return isClose(mob, target, closeEnough)
                && target instanceof Turtle turtle
                && Turtle.BABY_ON_LAND_SELECTOR.test(turtle)
                && isAttackable(mob, turtle, requireLineOfSight);
    }

    public static boolean isSameTypeAndFriendly(LivingEntity mob, LivingEntity other) {
        return mob.getType() == other.getType() && (mob.isAlliedTo(other) || mob.getTeam() == null && other.getTeam() == null);
    }

    public static Optional<LivingEntity> getLivingEntityFromUUID(ServerLevel level, UUID uuid) {
        return Optional.of(uuid).map(level::getEntity).filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isLookingAtMe(LivingEntity me, LivingEntity target, double offset) {
        Vec3 viewVector = target.getViewVector(1.0F).normalize();
        Vec3 eyeToMyEyeVector = target.getEyePosition().vectorTo(me.getEyePosition());
        double distance = eyeToMyEyeVector.length();
        eyeToMyEyeVector = eyeToMyEyeVector.normalize();
        double dot = viewVector.dot(eyeToMyEyeVector);
        return dot > 1.0D - offset / distance && target.hasLineOfSight(me);
    }

    public static <T extends Mob> InteractionResult interactOn(Player player, T entity, InteractionHand hand, TriFunction<T, Player, InteractionHand, InteractionResult> mobInteract){
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInHandCopy = itemInHand.copy();
        InteractionResult interactionResult = interact(entity, player, hand, mobInteract); // replaces Entity#interact(Player, InteractionHand);
        if (interactionResult.consumesAction()) {
            if (player.getAbilities().instabuild && itemInHand == player.getItemInHand(hand) && itemInHand.getCount() < itemInHandCopy.getCount()) {
                itemInHand.setCount(itemInHandCopy.getCount());
            }

            if (!player.getAbilities().instabuild && itemInHand.isEmpty()) {
                ForgeEventFactory.onPlayerDestroyItem(player, itemInHandCopy, hand);
            }
            return interactionResult;
        } else {
            if (!itemInHand.isEmpty()) {
                if (player.getAbilities().instabuild) {
                    itemInHand = itemInHandCopy;
                }

                InteractionResult livingEntityInteractionResult = itemInHand.interactLivingEntity(player, entity, hand);
                if (livingEntityInteractionResult.consumesAction()) {
                    if (itemInHand.isEmpty() && !player.getAbilities().instabuild) {
                        ForgeEventFactory.onPlayerDestroyItem(player, itemInHandCopy, hand);
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }

                    return livingEntityInteractionResult;
                }
            }

            return InteractionResult.PASS;
        }
    }

    private static <T extends Mob> InteractionResult interact(T mob, Player player, InteractionHand hand, TriFunction<T, Player, InteractionHand, InteractionResult> mobInteract) {
        if (!mob.isAlive()) {
            return InteractionResult.PASS;
        } else if (mob.getLeashHolder() == player) {
            mob.dropLeash(true, !player.getAbilities().instabuild);
            return InteractionResult.sidedSuccess(mob.level.isClientSide);
        } else {
            InteractionResult importantInteractionResult = ((MobAccessor)mob).callCheckAndHandleImportantInteractions(player, hand);
            if (importantInteractionResult != null && importantInteractionResult.consumesAction()) {
                return importantInteractionResult;
            } else {
                importantInteractionResult = mobInteract.apply(mob, player, hand);
                if (importantInteractionResult.consumesAction()) {
                    mob.gameEvent(GameEvent.ENTITY_INTERACT);
                    return importantInteractionResult;
                } else {
                    return InteractionResult.PASS;
                }
            }
        }
    }

    public static boolean isInjured(LivingEntity livingEntity){
        return livingEntity.getHealth() < livingEntity.getMaxHealth();
    }

    public static void animalEat(Animal animal, ItemStack stack) {
        if (animal.isFood(stack) && !animal.level.isClientSide) {
            playSoundEvent(animal, animal.getEatingSound(stack));

            float healAmount = 1.0F;
            FoodProperties foodProperties = stack.getFoodProperties(animal);
            if(foodProperties != null){
                healAmount = foodProperties.getNutrition();
                addEatEffect(animal, animal.level, foodProperties);
            }
            if(isInjured(animal)) animal.heal(healAmount);

            animal.ate();
        }
    }

    public static void playSoundEvent(LivingEntity livingEntity, SoundEvent soundEvent) {
        livingEntity.playSound(soundEvent, getSoundVolume(livingEntity), livingEntity.getVoicePitch());
    }

    public static void setWalkAndLookTargetMemories(LivingEntity mob, Entity target, float speedModifier, int closeEnough) {
        PositionTracker lookTarget = new EntityTracker(target, true);
        WalkTarget walkTarget = new WalkTarget(target, speedModifier, closeEnough);
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, lookTarget);
        mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity mob, BlockPos target, float speedModifier, int closeEnough) {
        PositionTracker lookTarget = new BlockPosTracker(target);
        WalkTarget walkTarget = new WalkTarget(target, speedModifier, closeEnough);
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, lookTarget);
        mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    public static boolean onCheckCooldown(ServerLevel level, long lastCheckTimestamp, long checkCooldown) {
        long ticksSinceLastCheck = level.getGameTime() - lastCheckTimestamp;
        return lastCheckTimestamp != 0 && ticksSinceLastCheck > 0 && ticksSinceLastCheck < checkCooldown;
    }

    public static void setItemPickupCooldown(LivingEntity mob, int itemPickupCooldownTicks) {
        mob.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, itemPickupCooldownTicks);
    }

    public static Optional<LivingEntity> getLivingEntityFromId(Level level, int id) {
        return Optional.ofNullable(level.getEntity(id)).filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast);
    }

    public static Optional<LivingEntity> getOwner(OwnableEntity ownableEntity) {
        return Optional.ofNullable(ownableEntity.getOwner()).filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast);
    }

    public static Optional<LivingEntity> getTarget(LivingEntity livingEntity){
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ?
                livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET) :
                Optional.ofNullable(livingEntity instanceof Mob mob ? mob.getTarget() : null);
    }

    public static Optional<Entity> getTargetedEntity(Entity looker, int distance) {
        Vec3 eyePosition = looker.getEyePosition();
        Vec3 viewVector = looker.getViewVector(1.0F).scale(distance);
        Vec3 targetPosition = eyePosition.add(viewVector);
        AABB searchBox = looker.getBoundingBox().expandTowards(viewVector).inflate(1.0D);
        int distanceSquared = distance * distance;
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(looker, eyePosition, targetPosition, searchBox, AiUtil::canHitEntity, distanceSquared);
        if (entityHitResult == null) {
            return Optional.empty();
        } else {
            return eyePosition.distanceToSqr(entityHitResult.getLocation()) > (double)distanceSquared ? Optional.empty() : Optional.of(entityHitResult.getEntity());
        }
    }

    private static boolean canHitEntity(Entity entity) {
        return !entity.isSpectator() && entity.isPickable();
    }

    public static HitResult getHitResult(Entity looker, int distance){
        Vec3 eyePosition = looker.getEyePosition();
        Vec3 viewVector = looker.getViewVector(1.0F).scale(distance);
        Vec3 targetPosition = eyePosition.add(viewVector);

        HitResult hitResult = looker.level.clip(new ClipContext(eyePosition, targetPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, looker));
        if (hitResult.getType() != HitResult.Type.MISS) {
            targetPosition = hitResult.getLocation();
        }
        AABB searchBox = looker.getBoundingBox().expandTowards(viewVector).inflate(1.0D);
        int distanceSquared = distance * distance;
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(looker, eyePosition, targetPosition, searchBox, AiUtil::canHitEntity, distanceSquared);

        if (entityHitResult != null) {
            hitResult = entityHitResult;
        }

        return hitResult;
    }

    public static void sendEatParticles(ServerLevel level, LivingEntity entity, ItemStack itemstack) {
        if (!itemstack.isEmpty()) {
            Vec3 dist = (new Vec3(
                    ((double) entity.getRandom().nextFloat() - 0.5D) * 0.1D,
                    Math.random() * 0.1D + 0.1D,
                    0.0D))
                    .xRot(-entity.getXRot() * ((float)Math.PI / 180F))
                    .yRot(-entity.getYRot() * ((float)Math.PI / 180F));
            level.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, itemstack),
                    entity.getX() + entity.getLookAngle().x / 2.0D,
                    entity.getY(),
                    entity.getZ() + entity.getLookAngle().z / 2.0D,
                    10,
                    dist.x,
                    dist.y + 0.05D,
                    dist.z,
                    0.0F);
        }
    }

    public static float getSoundVolume(LivingEntity livingEntity) {
        return ((LivingEntityAccessor) livingEntity).callGetSoundVolume();
    }

    private static boolean isMovingOnLand(LivingEntity entity) {
        return entity.isOnGround() && isMoving(entity) && !entity.isInWaterOrBubble();
    }

    private static boolean isMoving(LivingEntity entity) {
        return entity.getDeltaMovement().horizontalDistanceSqr() > NEAR_ZERO_DELTA_MOVEMENT;
    }

    private static boolean isMovingInWater(LivingEntity entity) {
        return entity.isInWaterOrBubble() && isMoving(entity);
    }

    public static boolean isMovingOnLandOrInWater(LivingEntity entity) {
        return (entity.isOnGround() || entity.isInWaterOrBubble()) && isMoving(entity);
    }

    public static boolean isEntityTargetableIgnoringLineOfSight(LivingEntity entity, LivingEntity target) {
        return entity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, target) ? TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT_IGNORE_INVISIBILITY_TESTING.test(entity, target) : TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(entity, target);
    }

    public static boolean isPathClear(LivingEntity entity, LivingEntity target, int horizontalDistance, int verticalDistance) {
        double zDiff = target.getZ() - entity.getZ();
        double xDiff = target.getX() - entity.getX();
        double ratio = zDiff / xDiff;

        for(int horizontalStep = 0; horizontalStep < horizontalDistance; ++horizontalStep) {
            double zStep = ratio == 0.0D ? 0.0D : zDiff * (double)((float)horizontalStep / horizontalDistance);
            double xStep = ratio == 0.0D ? xDiff * (double)((float)horizontalStep / horizontalDistance) : zStep / ratio;

            for(int verticalStep = 1; verticalStep < verticalDistance + 1; ++verticalStep) {
                if (!entity.level.getBlockState(new BlockPos(entity.getX() + xStep, entity.getY() + (double)verticalStep, entity.getZ() + zStep)).getMaterial().isReplaceable()) {
                    return false;
                }
            }
        }

        return true;
    }

    public static void dropItemAtPos(LivingEntity entity, BlockPos blockPos, ItemStack itemStack) {
        ItemEntity drop = new ItemEntity(entity.level, blockPos.getX(), blockPos.getY(), blockPos.getY(), itemStack);
        entity.level.addFreshEntity(drop);
    }
}
