package com.infamous.call_of_the_wild.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;

public interface CollaredMob {

    String COLLAR_COLOR_TAG = "CollarColor";

    default void addCollarColorSaveData(CompoundTag tag) {
        tag.putByte(COLLAR_COLOR_TAG, (byte)this.getCollarColor().getId());
    }

    default void readCollarColorSaveData(CompoundTag tag) {
        if (tag.contains(COLLAR_COLOR_TAG, Tag.TAG_ANY_NUMERIC)) {
            this.setCollarColor(DyeColor.byId(tag.getInt(COLLAR_COLOR_TAG)));
        }
    }

    DyeColor getCollarColor();

    void setCollarColor(DyeColor collarColor);
}
