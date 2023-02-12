package com.infamous.all_bark_all_bite.common.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class Beg<T extends LivingEntity> extends Behavior<T> {
    private final BiPredicate<T, ItemStack> isInteresting;
    private final BiConsumer<T, Boolean> toggleInterest;
    private final float lookDistance;

    public Beg(BiPredicate<T, ItemStack> isInteresting, BiConsumer<T, Boolean> toggleInterest, float lookDistance) {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 40, 80);
        this.isInteresting = isInteresting;
        this.toggleInterest = toggleInterest;
        this.lookDistance = lookDistance;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean checkExtraStartConditions(ServerLevel level, T mob) {
        return this.playerHoldingInteresting(this.getPlayerHoldWantedItem(mob).get(), mob);
    }

    @Override
    public void start(ServerLevel level, T mob, long gameTime) {
        this.toggleInterest.accept(mob, true);
    }

    @Override
    public void tick(ServerLevel level, T mob, long gameTime) {
        this.getPlayerHoldWantedItem(mob).ifPresent(player -> BehaviorUtils.lookAtEntity(mob, player));
    }

    @Override
    public boolean canStillUse(ServerLevel level, T mob, long gameTime) {
        Optional<Player> optionalPlayer = this.getPlayerHoldWantedItem(mob);
        if(optionalPlayer.isPresent()){
            Player player = optionalPlayer.get();
            if (!player.isAlive()) {
                return false;
            } else if (mob.distanceToSqr(player) > (double)(this.lookDistance * this.lookDistance)) {
                return false;
            } else {
                return this.playerHoldingInteresting(player, mob);
            }
        } else{
            return false;
        }
    }

    private boolean playerHoldingInteresting(Player player, T mob) {
        return player.isHolding(is -> this.isInteresting.test(mob, is));
    }

    private Optional<Player> getPlayerHoldWantedItem(T mob) {
        return mob.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    @Override
    public void stop(ServerLevel level, T mob, long gameTime) {
        this.toggleInterest.accept(mob, false);
    }
}
