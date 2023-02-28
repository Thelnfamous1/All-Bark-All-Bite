package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.mixin.LivingEntityAccessor;
import com.infamous.all_bark_all_bite.mixin.MobAccessor;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Optional;
import java.util.function.BiConsumer;

public class PickUpItemTrigger<E extends Mob> extends Behavior<E> {

    private final BiConsumer<E, ItemEntity> pickUpItem;

    public PickUpItemTrigger(BiConsumer<E, ItemEntity> pickUpItem) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT));
        this.pickUpItem = pickUpItem;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        ItemEntity nearestWantedItem = this.getNearestWantedItem(mob).get();
        Vec3i pickupReach = ((MobAccessor)mob).callGetPickupReach();
        if (mob.canPickUpLoot() && mob.isAlive() && !((LivingEntityAccessor)mob).getDead() && ForgeEventFactory.getMobGriefingEvent(level, mob)) {
            AABB pickupBox = mob.getBoundingBox().inflate(pickupReach.getX(), pickupReach.getY(), pickupReach.getZ());
            if(nearestWantedItem.getBoundingBox().intersects(pickupBox)){
                return !nearestWantedItem.isRemoved() && !nearestWantedItem.getItem().isEmpty() && !nearestWantedItem.hasPickUpDelay() && mob.wantsToPickUp(nearestWantedItem.getItem());
            }
        }
        return false;
    }

    private Optional<ItemEntity> getNearestWantedItem(E mob) {
        return mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.pickUpItem.accept(mob, getNearestWantedItem(mob).get());
    }
}
