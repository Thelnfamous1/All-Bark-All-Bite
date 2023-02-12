package com.infamous.all_bark_all_bite.common.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TickableEntityManager {
    default void tick(ServerLevel level) {
        this.tick(level, entity -> true, entity -> {
        });
    }

    default void tick(ServerLevel level, Predicate<Entity> isValid) {
        this.tick(level, isValid, entity -> {
        });
    }

    void tick(ServerLevel level, Predicate<Entity> isValid, Consumer<Entity> onInvalid);
}
