package com.infamous.all_bark_all_bite.common.goal;

import com.infamous.all_bark_all_bite.common.entity.LookTargetAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class LookAtTargetSinkGoal extends Goal {

    private final PathfinderMob mob;
    private final LookTargetAccessor lookTargetAccessor;
    private long endTimestamp;

    public LookAtTargetSinkGoal(PathfinderMob mob){
        this.mob = mob;
        this.lookTargetAccessor = LookTargetAccessor.cast(mob);
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.lookTargetAccessor.getLookTarget() != null;
    }

    @Override
    public void start() {
        int duration = 45 + this.mob.getRandom().nextInt(90 + 1 - 45);
        this.endTimestamp = this.mob.level.getGameTime() + (long)duration;
    }

    @Override
    public boolean canContinueToUse() {
        PositionTracker lookTarget = this.lookTargetAccessor.getLookTarget();
        return lookTarget != null && this.isVisibleBy(lookTarget) && this.mob.level.getGameTime() <= this.endTimestamp;
    }

    private boolean isVisibleBy(PositionTracker lookTarget) {
        if(lookTarget instanceof EntityTracker entityTracker){
            Entity entity = entityTracker.getEntity();
            if (entity instanceof LivingEntity living) {
                if (!living.isAlive()) {
                    return false;
                } else {
                    return this.mob.getSensing().hasLineOfSight(entity);
                }
            }
        }
        return true;
    }

    @Override
    public void tick() {
        PositionTracker lookTarget = this.lookTargetAccessor.getLookTarget();
        if(lookTarget != null) this.mob.getLookControl().setLookAt(lookTarget.currentPosition());
    }

    @Override
    public void stop() {
        this.lookTargetAccessor.setLookTarget(null);
    }
}
