package com.infamous.all_bark_all_bite.common.entity.houndmaster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.infamous.all_bark_all_bite.common.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.entity.illager_hound.IllagerHound;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABInstruments;
import com.infamous.all_bark_all_bite.common.util.DebugUtil;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class Houndmaster extends AbstractIllager implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_WHISTLING = SynchedEntityData.defineId(Houndmaster.class, EntityDataSerializers.BOOLEAN);

    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Houndmaster>>> SENSOR_TYPES =
            ImmutableList.of(
                    SensorType.NEAREST_LIVING_ENTITIES
            );
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES =
            ImmutableList.of(
                    MemoryModuleType.NEAREST_LIVING_ENTITIES,
                    MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES
            );
    private static final String SUMMONED_HOUNDS_TAG = "summonedHounds";
    private int whistleDelay;

    private boolean summonedHounds;

    public Houndmaster(EntityType<? extends Houndmaster> type, Level level) {
        super(type, level);
        this.whistleDelay = 20;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new Houndmaster.SummonHoundsGoal());
        this.goalSelector.addGoal(5, new RangedBowAttackGoal<>(this, 0.5D, 20, 15.0F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_WHISTLING, false);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<Houndmaster> getBrain() {
        return (Brain<Houndmaster>) super.getBrain();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(this.whistleDelay > 0){
            this.whistleDelay--;
        }
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("houndmasterBrain");
        this.getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
        if(this.level instanceof ServerLevel serverLevel){
            DebugUtil.sendEntityBrain(this, serverLevel);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(SUMMONED_HOUNDS_TAG, this.summonedHounds);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.summonedHounds = tag.getBoolean(SUMMONED_HOUNDS_TAG);
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if(this.isAggressive()){
            if (this.isHolding(is -> is.getItem() instanceof BowItem)) {
                return AbstractIllager.IllagerArmPose.BOW_AND_ARROW;
            } else {
                return AbstractIllager.IllagerArmPose.ATTACKING;
            }
        }
        return AbstractIllager.IllagerArmPose.NEUTRAL;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        RandomSource randomSource = levelAccessor.getRandom();
        this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
        this.populateDefaultEquipmentEnchantments(randomSource, difficultyInstance);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, tag);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
    protected void enchantSpawnedWeapon(RandomSource randomSource, float specialMultiplier) {
        super.enchantSpawnedWeapon(randomSource, specialMultiplier);
        if (MiscUtil.oneInChance(randomSource, 300)) {
            ItemStack mainHandItem = this.getMainHandItem();
            if (mainHandItem.is(Items.BOW)) {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(mainHandItem);
                map.putIfAbsent(Enchantments.PUNCH_ARROWS, 1);
                EnchantmentHelper.setEnchantments(map, mainHandItem);
                this.setItemSlot(EquipmentSlot.MAINHAND, mainHandItem);
            }
        }
    }

    @Override
    public boolean isAlliedTo(@Nullable Entity other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (super.isAlliedTo(other)) {
            return true;
        } else if (other instanceof OwnableEntity ownable) {
            return this.isAlliedTo(ownable.getOwner());
        } else if (other instanceof LivingEntity livingEntity && livingEntity.getMobType() == MobType.ILLAGER) {
            return this.getTeam() == null && other.getTeam() == null;
        } else {
            return false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void applyRaidBuffs(int groupsSpawned, boolean b) {
        Raid raid = this.getCurrentRaid();
        boolean enchant = this.random.nextFloat() <= raid.getEnchantOdds();
        if (enchant) {
            ItemStack bowStack = new ItemStack(Items.BOW);
            Map<Enchantment, Integer> map = Maps.newHashMap();
            Enchantment difficultyScaledEnchant = Enchantments.POWER_ARROWS;
            if (groupsSpawned > raid.getNumGroups(Difficulty.NORMAL)) {
                map.put(difficultyScaledEnchant, 2);
            } else if (groupsSpawned > raid.getNumGroups(Difficulty.EASY)) {
                map.put(difficultyScaledEnchant, 1);
            }
            map.put(Enchantments.PUNCH_ARROWS, 1);
            EnchantmentHelper.setEnchantments(map, bowStack);
            this.setItemSlot(EquipmentSlot.MAINHAND, bowStack);
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        InteractionHand weaponHoldingHand = ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem);
        ItemStack projectile = this.getProjectile(this.getItemInHand(weaponHoldingHand));
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectile, power);
        if (this.getItemInHand(weaponHoldingHand).getItem() instanceof BowItem bowItem) arrow = bowItem.customArrow(arrow);
        double xDist = target.getX() - this.getX();
        double yDist = target.getY(1.0D / 3) - arrow.getY();
        double zDist = target.getZ() - this.getZ();
        double horizontalDist = Math.sqrt(xDist * xDist + zDist * zDist);
        arrow.shoot(xDist, yDist + horizontalDist * 0.2D, zDist, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(arrow);
    }

    public void setWhistling(boolean whistling){
        this.entityData.set(DATA_WHISTLING, whistling);
    }

    public boolean isWhistling() {
        return this.entityData.get(DATA_WHISTLING);
    }

    class SummonHoundsGoal extends Goal {
        private static final int HOUNDS_TO_SUMMON = 3;
        private int houndsToSummon;
        private int whistleTicks;

        SummonHoundsGoal(){
            super();
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !Houndmaster.this.summonedHounds && Houndmaster.this.whistleDelay <= 0;
        }

        @Override
        public void start() {
            this.whistleTicks = this.adjustedTickDelay(ABABInstruments.WHISTLE_DURATION);
            this.houndsToSummon = HOUNDS_TO_SUMMON;
            Houndmaster.this.playSound(SoundEvents.NOTE_BLOCK_FLUTE, 1.0F, 1.0F);
            Houndmaster.this.setWhistling(true);
        }

        @Override
        public void tick() {
            --this.whistleTicks;
            if (this.whistleTicks == 0) {
                List<LivingEntity> nearbyMobs = GenericAi.getNearestLivingEntities(Houndmaster.this);
                for(LivingEntity nearbyMob : nearbyMobs){
                    if(this.houndsToSummon <= 0) break;
                    if(nearbyMob instanceof IllagerHound hound && hound.getOwnerUUID() == null){
                        hound.setOwner(Houndmaster.this);
                        this.houndsToSummon--;
                    }
                }
                this.summonRemainingHounds();
                Houndmaster.this.summonedHounds = true;
            }
        }

        private void summonRemainingHounds() {
            ServerLevel serverlevel = (ServerLevel) Houndmaster.this.level;
            for(int i = 0; i < this.houndsToSummon; ++i) {
                BlockPos blockPos = Houndmaster.this.blockPosition().offset(
                        -2 + Houndmaster.this.random.nextInt(5),
                        0,
                        -2 + Houndmaster.this.random.nextInt(5));
                MiscUtil.createEntity(ABABEntityTypes.ILLAGER_HOUND.get(), serverlevel).ifPresent(hound -> {
                    hound.moveTo(blockPos, Houndmaster.this.getYRot(), 0.0F);
                    hound.finalizeSpawn(serverlevel, Houndmaster.this.level.getCurrentDifficultyAt(blockPos), MobSpawnType.MOB_SUMMONED, null, null);
                    hound.setOwner(Houndmaster.this);
                    //hound.setLeashedTo(Houndmaster.this, true);
                    serverlevel.addFreshEntityWithPassengers(hound);
                });
            }
        }

        @Override
        public void stop() {
            Houndmaster.this.setWhistling(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public ItemStack getPickResult() {
        return ForgeSpawnEggItem.fromEntityType(this.getType()).getDefaultInstance();
    }
}
