package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class HurtByEntityTrigger<E extends LivingEntity> extends Behavior<E> {
    private final BiPredicate<E, LivingEntity> canUse;
    private final BiConsumer<E, LivingEntity> onHurtBy;
    private DamageSource lastHurtBy;

    public HurtByEntityTrigger(BiConsumer<E, LivingEntity> onHurtBy){
        this((m, le) -> true, onHurtBy);
    }

    public HurtByEntityTrigger(BiPredicate<E, LivingEntity> canUse, BiConsumer<E, LivingEntity> onHurtBy) {
        super(ImmutableMap.of(
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT
        ));
        this.canUse = canUse;
        this.onHurtBy = onHurtBy;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return this.isNotAlreadyReacting(mob) && this.canUse.test(mob, this.getHurtByEntity(mob).get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private boolean isNotAlreadyReacting(E mob) {
        DamageSource hurtBy = this.getHurtBy(mob).get();
        LivingEntity hurtByEntity = this.getHurtByEntity(mob).get();
        return this.lastHurtBy == null || this.lastHurtBy != hurtBy && hurtBy.getEntity() == hurtByEntity;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.lastHurtBy = this.getHurtBy(mob).get();
        this.onHurtBy.accept(mob, this.getHurtByEntity(mob).get());
    }

    private Optional<DamageSource> getHurtBy(LivingEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.HURT_BY);
    }

    private Optional<LivingEntity> getHurtByEntity(LivingEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
    }
}
