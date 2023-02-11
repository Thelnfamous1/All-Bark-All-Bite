package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Consumer;

public class HurtByTrigger<E extends LivingEntity> extends Behavior<E> {
    private final Consumer<E> onHurt;
    private DamageSource lastHurtBy;

    public HurtByTrigger(Consumer<E> onHurt) {
        super(ImmutableMap.of(
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT
        ));
        this.onHurt = onHurt;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return this.isNotAlreadyReacting(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private boolean isNotAlreadyReacting(E mob) {
        DamageSource hurtBy = this.getHurtBy(mob).get();
        return this.lastHurtBy == null || this.lastHurtBy != hurtBy;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.lastHurtBy = this.getHurtBy(mob).get();
        this.onHurt.accept(mob);
    }

    private Optional<DamageSource> getHurtBy(LivingEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.HURT_BY);
    }
}
