package com.infamous.call_of_the_wild.common.behavior.wolflike;

import com.infamous.call_of_the_wild.common.behavior.FleeSun;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;

@SuppressWarnings("unused")
public class SeekShelter<E extends PathfinderMob> extends FleeSun<E> {
    private static final int FIND_SHELTER_INTERVAL = 100;
    private int interval = AiUtil.reducedTickDelay(FIND_SHELTER_INTERVAL);

    public SeekShelter(float speedModifier) {
        super(speedModifier);
    }

    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (!mob.isSleeping()) {
            if (mob.level.isThundering() && mob.level.canSeeSky(mob.blockPosition())) {
                return this.setWantedPos(level, mob);
            } else if (this.interval > 0) {
                --this.interval;
                return false;
            } else {
                this.interval = FIND_SHELTER_INTERVAL;
                BlockPos blockPos = mob.blockPosition();
                return mob.level.isDay() && level.canSeeSky(blockPos) && !level.isVillage(blockPos) && this.setWantedPos(level, mob);
            }
        } else {
            return false;
        }
    }

    public void start(ServerLevel level, E mob, long gameTime) {
        //mob.clearStates();
        super.start(level, mob, gameTime);
    }
}