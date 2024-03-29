package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class UpdateTarget extends Behavior<Mob> {
    public UpdateTarget() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        LivingEntity attackTarget = GenericAi.getAttackTarget(mob).orElse(null);
        mob.setAggressive(attackTarget != null);
        if(attackTarget != mob.getTarget()) mob.setTarget(attackTarget);
    }
}
