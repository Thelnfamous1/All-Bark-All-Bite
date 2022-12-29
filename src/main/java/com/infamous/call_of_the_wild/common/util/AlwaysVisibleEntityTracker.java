package com.infamous.call_of_the_wild.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;

@SuppressWarnings("NullableProblems")
public class AlwaysVisibleEntityTracker extends EntityTracker {
    public AlwaysVisibleEntityTracker(Entity entity, boolean trackEyeHeight) {
        super(entity, trackEyeHeight);
    }

    @Override
    public boolean isVisibleBy(LivingEntity livingEntity) {
        return true;
    }

    @Override
    public String toString() {
        return "AlwaysVisible" + super.toString();
    }
}
