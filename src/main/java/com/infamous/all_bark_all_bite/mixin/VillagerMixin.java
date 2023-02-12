package com.infamous.all_bark_all_bite.mixin;


import com.infamous.all_bark_all_bite.common.event.BrainEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {
    public VillagerMixin(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
    }

    @Inject(method = "refreshBrain", at = @At("RETURN"))
    private void handleRefreshBrain(ServerLevel level, CallbackInfo ci){
        BrainEvent.VillagerRefresh event = new BrainEvent.VillagerRefresh(this.cast());
        MinecraftForge.EVENT_BUS.post(event);
        this.brain = event.getNewBrain();
    }

    private Villager cast() {
        return (Villager) (Object) this;
    }
}
