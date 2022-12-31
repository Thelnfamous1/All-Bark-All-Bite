package com.infamous.call_of_the_wild.common.behavior.sleep;

import com.infamous.call_of_the_wild.common.behavior.FleeSun;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;

@SuppressWarnings("unused")
public class SeekShelter<E extends PathfinderMob> extends FleeSun<E> {
    private static final long CHECK_COOLDOWN = 40L;
    private long lastCheckTimestamp;

    public SeekShelter(float speedModifier) {
        super(speedModifier);
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (!mob.isSleeping()) {
            if (mob.level.isThundering() && mob.level.canSeeSky(mob.blockPosition())) {
                return this.setWantedPos(level, mob);
            } else {
                if (level.getGameTime() - this.lastCheckTimestamp < CHECK_COOLDOWN) {
                    return false;
                } else {
                    this.lastCheckTimestamp = level.getGameTime();
                    BlockPos blockPos = mob.blockPosition();
                    return mob.level.isDay() && level.canSeeSky(blockPos) && !level.isVillage(blockPos) && this.setWantedPos(level, mob);
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        //mob.clearStates();
        super.start(level, mob, gameTime);
    }
}