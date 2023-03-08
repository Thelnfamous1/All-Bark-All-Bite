package com.infamous.all_bark_all_bite.common.compat;

import com.infamous.all_bark_all_bite.common.ABABTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class FDCompat {
    public static final TagKey<EntityType<?>> DOG_FOOD_USERS = ABABTags.createEntityTypeTag(new ResourceLocation(CompatUtil.FARMERS_DELIGHT_MODID, "dog_food_users"));
}
