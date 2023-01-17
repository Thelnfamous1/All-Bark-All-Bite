package com.infamous.call_of_the_wild.common.behavior.pet;

import com.infamous.call_of_the_wild.common.behavior.TargetBehavior;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class OwnerHurtByTarget extends TargetBehavior<TamableAnimal> {
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public OwnerHurtByTarget() {
        super(false);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TamableAnimal tamable) {
        if (tamable.isTame() && !tamable.isOrderedToSit()) {
            Optional<LivingEntity> maybeOwner = AiUtil.getOwner(tamable);
            if (maybeOwner.isEmpty()) {
                return false;
            } else {
                LivingEntity owner = maybeOwner.get();
                this.ownerLastHurtBy = owner.getLastHurtByMob();
                int lastHurtByMobTimestamp = owner.getLastHurtByMobTimestamp();
                return lastHurtByMobTimestamp != this.timestamp
                        && this.canAttack(tamable, this.ownerLastHurtBy, TargetingConditions.DEFAULT)
                        && tamable.wantsToAttack(this.ownerLastHurtBy, owner);
            }
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel level, TamableAnimal tamable, long gameTime) {
        StartAttacking.setAttackTarget(tamable, this.ownerLastHurtBy);
        AiUtil.getOwner(tamable).ifPresent(owner -> this.timestamp = owner.getLastHurtByMobTimestamp());
        super.start(level, tamable, gameTime);
    }
}
