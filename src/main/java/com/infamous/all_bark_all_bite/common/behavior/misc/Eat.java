package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class Eat extends Behavior<Animal> {
    private final Consumer<Animal> onEaten;
    private int eatRemainingTicks;

    public Eat(Consumer<Animal> onEaten) {
        super(ImmutableMap.of(
                MemoryModuleType.ATE_RECENTLY, MemoryStatus.VALUE_ABSENT
        ));
        this.onEaten = onEaten;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Animal animal) {
        if(animal.isUsingItem()){
            return false;
        }
        ItemStack eatItem = this.getEatItem(animal);
        return this.canEat(animal, eatItem);
    }

    private ItemStack getEatItem(Animal animal) {
        return animal.getItemInHand(InteractionHand.MAIN_HAND);
    }

    private boolean canEat(Animal animal, ItemStack itemStack) {
        return animal.isFood(itemStack)
                && !animal.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && animal.onGround()
                && !animal.isSleeping();
    }

    @Override
    protected void start(ServerLevel level, Animal animal, long gameTime) {
        int useDuration = this.getEatItem(animal).getUseDuration();
        // Clamp use duration between vanilla food eat durations of 16 (fast food) and 32 (non-fast food)
        useDuration = Mth.clamp(useDuration, 16, 32);
        this.eatRemainingTicks = useDuration;
        animal.startUsingItem(InteractionHand.MAIN_HAND);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Animal animal, long gameTime) {
        ItemStack eatItem = this.getEatItem(animal);
        if(!animal.isUsingItem() || animal.getUseItem() != eatItem){
            return false;
        }
        return this.canEat(animal, eatItem) && this.eatRemainingTicks >= 0;
    }

    @Override
    protected void tick(ServerLevel level, Animal animal, long gameTime) {
        // This will allow us to finish using the item a tick before LivingEntity#completeUsingItem would be called
        --this.eatRemainingTicks;
        if (this.eatRemainingTicks <= 0) {
            animal.stopUsingItem();
            ItemStack eatItem = this.getEatItem(animal);
            AiUtil.animalEat(animal, eatItem);
            eatItem.shrink(1);
            this.onEaten.accept(animal);
        }
    }

    @Override
    protected void stop(ServerLevel level, Animal animal, long gameTime) {
        if(animal.getUseItem() == this.getEatItem(animal)){
            animal.stopUsingItem();
        }
    }
}
