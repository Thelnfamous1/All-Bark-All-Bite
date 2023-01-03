package com.infamous.call_of_the_wild.common.entity.dog.vibration;

import com.infamous.call_of_the_wild.common.entity.dog.ai.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.COTWGameEvents;
import com.infamous.call_of_the_wild.common.sensor.vibration.EntityVibrationListenerConfig;
import com.infamous.call_of_the_wild.common.util.PackAi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SharedWolfVibrationListenerConfig<T extends TamableAnimal> extends EntityVibrationListenerConfig<T> {

    @Override
    protected boolean shouldEntityListen(ServerLevel level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
        if(!this.entity.isNoAi()
                && !this.entity.isDeadOrDying()
                //&& !this.entity.getBrain().hasMemoryValue(MemoryModuleType.VIBRATION_COOLDOWN)
                && level.getWorldBorder().isWithinBounds(blockPos)
                && !this.entity.isRemoved()
                && this.entity.getLevel() == level){
            Optional<GlobalPos> howlLocation = SharedWolfAi.getHowlLocation(this.entity);
            if (howlLocation.isEmpty()) {
                return true;
            } else {
                GlobalPos howlPos = howlLocation.get();
                return howlPos.dimension().equals(level.dimension()) && howlPos.pos().equals(blockPos);
            }
        }
        return false;
    }

    @Override
    protected void onEntityReceiveSignal(ServerLevel level, GameEventListener gameEventListener, BlockPos signalPos, GameEvent signalEvent, @Nullable Entity signalSender, @Nullable Entity signalSenderOwner, float signalDistance) {
        //this.entity.getBrain().setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);

        if (signalEvent == COTWGameEvents.ENTITY_HOWL.get()
                && signalSender != this.entity
                && signalSender instanceof LivingEntity howler) {
            if(!this.wantsToRespond(howler)) return;

            // leaders only respond verbally
            if(this.canTreatAsFollower(howler)){
                this.respondToHowl(true);
            }
            // followers both follow the signal and respond verbally
            else if(this.isFollowerOf(howler)){
                SharedWolfAi.followHowl(this.entity, signalPos);
                this.respondToHowl(false);
            }
            // loners both follow the signal and respond verbally
            else if(this.canJoinOrCreatePackWith(howler)){
                SharedWolfAi.followHowl(this.entity, signalPos);
                this.respondToHowl(false);
            }
        }
    }

    private boolean wantsToRespond(LivingEntity other){
        return !this.entity.closerThan(other, SharedWolfAi.ADULT_FOLLOW_RANGE.getMaxValue());
    }

    private boolean canJoinOrCreatePackWith(LivingEntity other){
        return PackAi.isLoner(this.entity) && (PackAi.canAddToFollowers(other, this.entity) || PackAi.isLoner(other));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private boolean isFollowerOf(LivingEntity other) {
        return PackAi.isFollower(this.entity) && PackAi.getLeaderUUID(this.entity).get().equals(other.getUUID());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private boolean canTreatAsFollower(LivingEntity other) {
        return PackAi.hasFollowers(this.entity) && (PackAi.getFollowers(this.entity).get().contains(other) || PackAi.canAddToFollowers(this.entity, other));
    }

    private void respondToHowl(boolean ignoreCooldown) {
        if(!SharedWolfAi.hasHowledRecently(this.entity) || ignoreCooldown){
            SharedWolfAi.howl(this.entity);
            SharedWolfAi.setHowledRecently(this.entity, SharedWolfAi.TIME_BETWEEN_HOWLS.sample(this.entity.getRandom()));
        }
    }

}
