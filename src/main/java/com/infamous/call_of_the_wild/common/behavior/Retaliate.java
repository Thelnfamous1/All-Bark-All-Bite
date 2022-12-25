package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@SuppressWarnings("NullableProblems")
public class Retaliate<E extends Mob> extends Behavior<E> {
    private final BiPredicate<E, LivingEntity> canRetaliate;
    private final BiConsumer<E, LivingEntity> onHurtBy;

    public Retaliate(BiConsumer<E, LivingEntity> onHurtBy){
        this((m, le) -> true, onHurtBy);
    }

    public Retaliate(BiPredicate<E, LivingEntity> canRetaliate, BiConsumer<E, LivingEntity> onHurtBy) {
        super(ImmutableMap.of(
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT
        ));
        this.canRetaliate = canRetaliate;
        this.onHurtBy = onHurtBy;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        LivingEntity hurtByEntity = getHurtByEntity(mob);
        return this.isNotAlreadyRetaliatingAgainst(mob, hurtByEntity) && this.canRetaliate.test(mob, hurtByEntity);
    }

    private boolean isNotAlreadyRetaliatingAgainst(E mob, LivingEntity hurtByEntity) {
        DamageSource lastDamageSource = mob.getLastDamageSource();
        return lastDamageSource == null || lastDamageSource.getEntity() != hurtByEntity;
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.onHurtBy.accept(mob, getHurtByEntity(mob));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static LivingEntity getHurtByEntity(LivingEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).get();
    }
}
