package com.infamous.all_bark_all_bite.common.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface AnimalAccessor {

    InteractionResult animalInteract(Player player, InteractionHand hand);

    void takeItemFromPlayer(Player player, InteractionHand hand, ItemStack itemStack);

    static AnimalAccessor cast(Animal animal){
        return (AnimalAccessor) animal;
    }
}
