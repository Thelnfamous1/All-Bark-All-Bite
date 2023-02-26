package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.AngerAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class UpdateNeutralMob<T extends Mob & NeutralMob> extends Behavior<T> {
    public UpdateNeutralMob() {
        super(ImmutableMap.of(
                MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected void start(ServerLevel level, T mob, long gameTime) {
        LivingEntity angerTarget = AngerAi.getAngerTarget(mob).orElse(null);
        if(angerTarget != null && mob.getTarget() != null && angerTarget != mob.getTarget()){
            mob.setTarget(angerTarget);
            mob.setPersistentAngerTarget(angerTarget.getUUID());
        }
        int angryAtTime = AngerAi.getAngryAtTime(mob);
        if(mob.getRemainingPersistentAngerTime() != angryAtTime) {
            mob.setRemainingPersistentAngerTime(angryAtTime);
        }
    }
}
