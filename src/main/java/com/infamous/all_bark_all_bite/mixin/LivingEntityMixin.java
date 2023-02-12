package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.common.event.BrainEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow protected Brain<?> brain;

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handleInitBrain(EntityType<?> type, Level level, CallbackInfo ci){
        BrainEvent.MakeBrain event = new BrainEvent.MakeBrain(this.cast());
        MinecraftForge.EVENT_BUS.post(event);
        this.brain = event.getNewBrain();
    }

    private LivingEntity cast() {
        return (LivingEntity) (Object) this;
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "RETURN"))
    private void handleReadBrainData(CompoundTag tag, CallbackInfo ci){
        if (tag.contains("Brain", 10)) {
            BrainEvent.MakeBrain event = new BrainEvent.MakeBrain(this.cast(), tag.get("Brain"));
            MinecraftForge.EVENT_BUS.post(event);
            this.brain = event.getNewBrain();
        }
    }
}
