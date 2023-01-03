package com.infamous.call_of_the_wild.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.entity.dog.ai.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.PackAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"NullableProblems", "unused"})
public class StartHowling<E extends LivingEntity> extends Behavior<E> {
    private final UniformInt howlCooldown;
    private final int tooFar;

    public StartHowling(UniformInt howlCooldown, int tooFar) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.LEADER.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.FOLLOWERS.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.HOWLED_RECENTLY.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.howlCooldown = howlCooldown;
        this.tooFar = tooFar;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if(PackAi.isFollower(mob)){
            Optional<LivingEntity> leader = PackAi.getLeader(mob);
            return leader.isPresent() && this.followerTooFar(leader.get(), mob);
        } else if(PackAi.hasFollowers(mob)){
            Set<LivingEntity> followers = PackAi.getFollowers(mob).get();
            for(LivingEntity follower : followers){
                if(follower == mob) continue; // ignore self
                if(this.followerTooFar(mob, follower)){
                    return true;
                }
            }
        }
        return true;
    }

    private boolean followerTooFar(LivingEntity leader, LivingEntity follower) {
        return !follower.closerThan(leader, this.tooFar);
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        SharedWolfAi.howl(mob);
        int howlCooldownInTicks = this.howlCooldown.sample(mob.getRandom());
        SharedWolfAi.setHowledRecently(mob, howlCooldownInTicks);
        //GenericAi.getNearbyAllies(mob).forEach(le -> SharedWolfAi.setHowledRecently(le, this.howlCooldown.sample(le.getRandom())));
    }
}
