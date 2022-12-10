package com.infamous.call_of_the_wild.common.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

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
            LivingEntity owner = tamable.getOwner();
            if (owner == null) {
                return false;
            } else {
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
        LivingEntity owner = tamable.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start(level, tamable, gameTime);
    }
}
