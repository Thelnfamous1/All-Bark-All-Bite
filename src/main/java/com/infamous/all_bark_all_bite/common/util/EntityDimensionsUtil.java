package com.infamous.all_bark_all_bite.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

public class EntityDimensionsUtil {
    public static EntityDimensions resetIfSleeping(Entity entity, EntityDimensions originalSize) {
        if(entity.hasPose(Pose.SLEEPING)){
            originalSize = entity.getDimensions(Pose.STANDING);
        }
        return originalSize;
    }

    public static EntityDimensions unfixIfNeeded(EntityDimensions originalSize) {
        if(originalSize.fixed){
            originalSize = new EntityDimensions(originalSize.width, originalSize.height, false);
        }
        return originalSize;
    }

    public static EntityDimensions resizeForLongJumpIfNeeded(Entity entity, EntityDimensions originalSize, float longJumpingScale) {
        if(entity.hasPose(Pose.LONG_JUMPING)){
            originalSize = originalSize.scale(longJumpingScale);
        }
        return originalSize;
    }
}
