package com.infamous.call_of_the_wild.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.ai.PackAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@SuppressWarnings("NullableProblems")
public class ValidateFollowers extends Behavior<LivingEntity> {

    private static final int CLOSE_ENOUGH_TO_RECALL = 64;

    public ValidateFollowers() {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.FOLLOWERS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity leader) {
        return PackAi.hasFollowers(leader);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, LivingEntity leader, long gameTime) {
        PackAi.getFollowerManager(leader).get().tick(level, e -> this.isValidFollower(e, leader), this::onFollowerInvalid);
    }

    private boolean isValidFollower(Entity entity, LivingEntity leader){
        if(entity instanceof LivingEntity follower){
            return !follower.isDeadOrDying()
                    && follower.level == leader.level
                    && PackAi.isFollower(follower)
                    && AiUtil.canBeConsideredAnAlly(leader, follower)
                    && follower.closerThan(leader, CLOSE_ENOUGH_TO_RECALL)
                    && leader.level.getWorldBorder().isWithinBounds(follower.getBoundingBox());
        }
        return false;
    }

    private void onFollowerInvalid(Entity entity){
        if(entity instanceof LivingEntity follower){
            PackAi.eraseLeader(follower);
        }
    }
}
