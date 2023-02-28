package com.infamous.all_bark_all_bite.common.entity;

import net.minecraft.world.entity.LivingEntity;

public interface AnimationControllerAccess<EAC extends EntityAnimationController<?>> {

    @SuppressWarnings("unchecked")
    static <EAC extends EntityAnimationController<?>> AnimationControllerAccess<EAC> cast(LivingEntity entity){
        return (AnimationControllerAccess<EAC>) entity;
    }

    EAC getAnimationController();
}
