package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.Iterables;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class COTWUtil {

    public static <T> T getRandomObject(Collection<T> from, RandomSource randomSource) {
        int index = randomSource.nextInt(from.size());
        return Iterables.get(from, index);
    }

    public static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        ItemStack singleton = stack.split(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }

        return singleton;
    }
}
