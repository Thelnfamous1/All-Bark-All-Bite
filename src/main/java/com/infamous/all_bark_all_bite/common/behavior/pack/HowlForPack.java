package com.infamous.all_bark_all_bite.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.logic.entity_manager.MultiEntityManager;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ai.PackAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Predicate;

public class HowlForPack<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> wantsToHowl;
    private final UniformInt howlCooldown;
    private final int tooFar;
    private long lastCheckTimestamp;

    public HowlForPack(Predicate<E> wantsToHowl, UniformInt howlCooldown, int tooFar) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.LEADER.get(), MemoryStatus.REGISTERED,
                ABABMemoryModuleTypes.FOLLOWERS.get(), MemoryStatus.REGISTERED,
                ABABMemoryModuleTypes.HOWLED_RECENTLY.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.wantsToHowl = wantsToHowl;
        this.howlCooldown = howlCooldown;
        this.tooFar = tooFar;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if(!this.wantsToHowl.test(mob)){
            return false;
        } else if(AiUtil.onCheckCooldown(level, this.lastCheckTimestamp, JoinOrCreatePackAndFollow.INTERVAL_TICKS)){
            return false;
        } else{
            if(PackAi.isFollower(mob)){
                this.timestampLastCheck(level.getGameTime());

                Optional<LivingEntity> leader = PackAi.getLeader(mob);
                return leader.isPresent() && this.followerTooFar(leader.get(), mob);
            } else if(PackAi.hasFollowers(mob)){
                this.timestampLastCheck(level.getGameTime());

                MultiEntityManager followers = PackAi.getFollowerManager(mob).get();
                return followers.stream().anyMatch(e -> this.followerTooFar(mob, e));
            }
            return true;
        }
    }

    private void timestampLastCheck(long gameTime) {
        this.lastCheckTimestamp = gameTime;
    }

    private boolean followerTooFar(LivingEntity leader, Entity follower) {
        return !follower.closerThan(leader, this.tooFar);
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        SharedWolfAi.howl(mob);
        int howlCooldownInTicks = this.howlCooldown.sample(mob.getRandom());
        SharedWolfAi.setHowledRecently(mob, howlCooldownInTicks);
    }
}
