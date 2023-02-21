package com.infamous.all_bark_all_bite.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.ai.PackAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ValidateLeader extends Behavior<LivingEntity> {

    public ValidateLeader() {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.LEADER.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity follower) {
        return PackAi.isFollower(follower);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, LivingEntity follower, long gameTime) {
        PackAi.getLeaderManager(follower).get().tick(level, e -> this.isValidLeader(e, follower), e -> this.onLeaderInvalid(e, follower));
    }

    private boolean isValidLeader(Entity entity, LivingEntity follower){
        if(entity instanceof LivingEntity leader){
            return !leader.isDeadOrDying()
                    && follower.level == leader.level
                    && !PackAi.isFollower(leader)
                    && AiUtil.isSameTypeAndFriendly(follower, leader)
                    && follower.closerThan(leader, follower.getAttributeValue(Attributes.FOLLOW_RANGE))
                    && follower.level.getWorldBorder().isWithinBounds(leader.getBoundingBox());
        }
        return false;
    }

    private void onLeaderInvalid(Entity entity, LivingEntity follower){
        if(entity instanceof LivingEntity){
            PackAi.eraseLeader(follower);
        }
    }
}
