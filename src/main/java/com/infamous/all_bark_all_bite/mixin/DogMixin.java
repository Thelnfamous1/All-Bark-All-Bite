package com.infamous.all_bark_all_bite.mixin;

import baguchan.revampedwolf.api.IHasInventory;
import baguchan.revampedwolf.api.IOpenWolfContainer;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("UnusedMixin")
@Mixin(Dog.class)
public abstract class DogMixin extends Wolf implements HasCustomInventoryScreen, IHasInventory {

    protected DogMixin(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level().isClientSide && player instanceof IOpenWolfContainer openWolfContainer) {
            openWolfContainer.openWolfInventory(this, this.getContainer());
        }
    }
}
