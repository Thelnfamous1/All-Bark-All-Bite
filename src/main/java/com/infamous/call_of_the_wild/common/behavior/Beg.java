package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.entity.InterestedMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class Beg<T extends LivingEntity & InterestedMob> extends Behavior<T> {
    private final float lookDistance;

    public Beg(float lookDistance) {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 40, 80);
        this.lookDistance = lookDistance;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean checkExtraStartConditions(ServerLevel level, T mob) {
        return playerHoldingInteresting(this.getPlayerHoldWantedItem(mob).get(), mob);
    }

    @Override
    public void start(ServerLevel level, T mob, long gameTime) {
        mob.setIsInterested(true);
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
                return playerHoldingInteresting(player, mob);
            }
        } else{
            return false;
        }
    }

    private Optional<Player> getPlayerHoldWantedItem(T mob) {
        return mob.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    @Override
    public void stop(ServerLevel level, T mob, long gameTime) {
        mob.setIsInterested(false);
    }

    public static <T extends LivingEntity & InterestedMob> boolean playerHoldingInteresting(Player player, T mob) {
        return player.isHolding(mob::isInteresting);
    }
}
