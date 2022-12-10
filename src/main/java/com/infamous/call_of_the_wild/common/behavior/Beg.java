package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.entity.InterestedMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class Beg<T extends Animal & InterestedMob> extends Behavior<T> {
    private final float lookDistance;

    public Beg(float lookDistance) {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 40, 80);
        this.lookDistance = lookDistance;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean checkExtraStartConditions(ServerLevel level, T animal) {
        return playerHoldingInteresting(this.getPlayerHoldWantedItem(animal).get(), animal);
    }

    @Override
    public void start(ServerLevel level, T animal, long gameTime) {
        animal.setIsInterested(true);
    }

    @Override
    public void tick(ServerLevel level, T animal, long gameTime) {
        this.getPlayerHoldWantedItem(animal).ifPresent(player -> BehaviorUtils.lookAtEntity(animal, player));
    }

    @Override
    public boolean canStillUse(ServerLevel level, T animal, long gameTime) {
        Optional<Player> optionalPlayer = this.getPlayerHoldWantedItem(animal);
        if(optionalPlayer.isPresent()){
            Player player = optionalPlayer.get();
            if (!player.isAlive()) {
                return false;
            } else if (animal.distanceToSqr(player) > (double)(this.lookDistance * this.lookDistance)) {
                return false;
            } else {
                return playerHoldingInteresting(player, animal);
            }
        } else{
            return false;
        }
    }

    private Optional<Player> getPlayerHoldWantedItem(T animal) {
        return animal.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    @Override
    public void stop(ServerLevel level, T animal, long gameTime) {
        animal.setIsInterested(false);
    }

    public static <T extends Animal & InterestedMob> boolean playerHoldingInteresting(Player player, T animal) {
        for(InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (animal.isInteresting(stack)) {
                return true;
            }
        }

        return false;
    }
}
