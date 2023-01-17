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
public class OwnerHurtTarget extends TargetBehavior<TamableAnimal> {
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public OwnerHurtTarget() {
        super(false);
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, TamableAnimal tamable) {
        if (tamable.isTame() && !tamable.isOrderedToSit()) {
            Optional<LivingEntity> maybeOwner = AiUtil.getOwner(tamable);
            if (maybeOwner.isEmpty()) {
                return false;
            } else {
                LivingEntity owner = maybeOwner.get();
                this.ownerLastHurt = owner.getLastHurtMob();
                int lastHurtMobTimestamp = owner.getLastHurtMobTimestamp();
                return lastHurtMobTimestamp != this.timestamp
                        && this.canAttack(tamable, this.ownerLastHurt, TargetingConditions.DEFAULT)
                        && tamable.wantsToAttack(this.ownerLastHurt, owner);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start(ServerLevel level, TamableAnimal tamable, long gameTime) {
        StartAttacking.setAttackTarget(tamable, this.ownerLastHurt);
        AiUtil.getOwner(tamable).ifPresent(owner -> this.timestamp = owner.getLastHurtMobTimestamp());
        super.start(level, tamable, gameTime);
    }

}
