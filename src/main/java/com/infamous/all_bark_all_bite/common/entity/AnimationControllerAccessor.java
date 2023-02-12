package com.infamous.all_bark_all_bite.common.entity;

import net.minecraft.world.entity.LivingEntity;

public interface AnimationControllerAccessor<EAC extends EntityAnimationController<?>> {

    @SuppressWarnings("unchecked")
    static <EAC extends EntityAnimationController<?>> AnimationControllerAccessor<EAC> cast(LivingEntity entity){
        return (AnimationControllerAccessor<EAC>) entity;
    }

    EAC getAnimationController();
}
