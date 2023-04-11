package com.infamous.all_bark_all_bite.client.compat;

import com.infamous.all_bark_all_bite.common.compat.CompatUtil;
import me.gentworm.storymod.accessor.WolfAccessor;
import me.gentworm.storymod.config.StoryModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import org.jetbrains.annotations.Nullable;

public class SMCompatClient {
    private static final ResourceLocation BLACK_WOLF_TEXTURES = new ResourceLocation(CompatUtil.STORY_MOD_MODID, "textures/entity/black_wolf/black_wolf.png");
    private static final ResourceLocation TAMED_WOLF_TEXTURES = new ResourceLocation(CompatUtil.STORY_MOD_MODID, "textures/entity/black_wolf/black_wolf_tamed.png");
    private static final ResourceLocation ANGRY_WOLF_TEXTURES = new ResourceLocation(CompatUtil.STORY_MOD_MODID, "textures/entity/black_wolf/black_wolf_angry.png");


    @Nullable
    public static ResourceLocation getTextureLocation(Wolf wolf) {
        if (StoryModConfig.CLIENT.blackWolfVariants.get() && ((WolfAccessor)wolf).isBlackWolf()) {
            if (wolf.isTame()) {
                return TAMED_WOLF_TEXTURES;
            } else {
                return wolf.isAngry() ? ANGRY_WOLF_TEXTURES : BLACK_WOLF_TEXTURES;
            }
        }
        return null;
    }
}
