package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;
import java.util.function.Predicate;

public class UpdateUnitMemory<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> predicate;
    private final MemoryModuleType<Unit> memory;

    public UpdateUnitMemory(Predicate<E> predicate, MemoryModuleType<Unit> memory) {
        super(ImmutableMap.of());
        this.predicate = predicate;
        this.memory = memory;
    }

    @Override
    protected void start(ServerLevel level, E entity, long gameTime) {
        Optional<Unit> current = entity.getBrain().getMemory(this.memory);
        if(this.predicate.test(entity)){
            if(current.isEmpty()) entity.getBrain().setMemory(this.memory, Unit.INSTANCE);
        } else if(current.isPresent()){
            entity.getBrain().eraseMemory(this.memory);
        }
    }
}
