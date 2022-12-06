package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.Iterables;
import net.minecraft.util.RandomSource;

import java.util.Collection;

public class Helper {

    public static <T> T getRandomObject(Collection<T> from, RandomSource randomSource) {
        int index = randomSource.nextInt(from.size());
        return Iterables.get(from, index);
    }
}
