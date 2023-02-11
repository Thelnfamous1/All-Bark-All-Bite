package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class Eat extends Behavior<Animal> {
    private final Consumer<Animal> onEaten;
    private int eatRemainingTicks;
    private final int eatDuration;

    public Eat(Consumer<Animal> onEaten, int eatDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.ATE_RECENTLY, MemoryStatus.VALUE_ABSENT
        ));
        this.onEaten = onEaten;
        this.eatDuration = eatDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Animal animal) {
        ItemStack itemStack = animal.getItemBySlot(EquipmentSlot.MAINHAND);
        return this.canEat(animal, itemStack);
    }

    @Override
    protected void start(ServerLevel level, Animal animal, long gameTime) {
        this.eatRemainingTicks = eatDuration;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Animal animal, long gameTime) {
        ItemStack itemStack = animal.getItemBySlot(EquipmentSlot.MAINHAND);
        return this.canEat(animal, itemStack) && this.eatRemainingTicks >= 0;
    }

    @Override
    protected void tick(ServerLevel level, Animal animal, long gameTime) {
        ItemStack itemStack = animal.getItemBySlot(EquipmentSlot.MAINHAND);
        if (this.eatRemainingTicks <= 0) {
            AiUtil.animalEat(animal, itemStack);
            AiUtil.sendEatParticles(level, animal, itemStack);
            itemStack.shrink(1);
            this.onEaten.accept(animal);
        } else if (this.eatRemainingTicks % 10 == 0) {
            AiUtil.playSoundEvent(animal, animal.getEatingSound(itemStack));
            AiUtil.sendEatParticles(level, animal, itemStack);
        }
        this.eatRemainingTicks--;
    }

    private boolean canEat(Animal animal, ItemStack itemStack) {
        return animal.isFood(itemStack)
                && !animal.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && animal.isOnGround()
                && !animal.isSleeping();
    }
}
