package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private EntityDimensions dimensions;

    @Shadow private float eyeHeight;

    @Shadow public abstract EntityType<?> getType();

    @Shadow public abstract Pose getPose();

    @Shadow protected abstract float getEyeHeight(Pose p_19976_, EntityDimensions p_19977_);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void handleEntityConstruct(EntityType<?> type, Level level, CallbackInfo ci){
        this.all_bark_all_bite_handleResize();
    }

    @Inject(method = "refreshDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;reapplyPosition()V"))
    private void handleChangeEntityDimensions(CallbackInfo ci){
        this.all_bark_all_bite_handleResize();
    }

    @Unique
    private void all_bark_all_bite_handleResize() {
        if(WolfHooks.canWolfChange(this.getType(), false, true)){
            EntityDimensions resize = WolfHooks.onWolfSize((Entity)(Object)this, this.dimensions);
            this.dimensions = resize;
            Pose pose = this.getPose();
            this.eyeHeight = this.getEyeHeight(pose, resize);
        }
    }
}
