package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.infamous.all_bark_all_bite.common.behavior.TargetBehavior;
import com.infamous.all_bark_all_bite.common.entity.OwnableMob;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.Optional;

public class CopyOwnerTarget<T extends Mob & OwnableMob> extends TargetBehavior<T> {
    private static final TargetingConditions COPY_OWNER_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting();

    public CopyOwnerTarget() {
        super(false);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T mob) {
        Optional<LivingEntity> owner = AiUtil.getOwner(mob);
        if(owner.isEmpty()){
            return false;
        } else{
            Optional<LivingEntity> target = AiUtil.getTarget(owner.get());
            return target.isPresent() && this.canAttack(mob, target.get(), COPY_OWNER_TARGETING);
        }
    }

    @Override
    protected void start(ServerLevel level, T mob, long gameTime) {
        AiUtil.getOwner(mob)
                .flatMap(AiUtil::getTarget)
                .ifPresent(target -> StartAttacking.setAttackTarget(mob, target));
        super.start(level, mob, gameTime);
    }
}
