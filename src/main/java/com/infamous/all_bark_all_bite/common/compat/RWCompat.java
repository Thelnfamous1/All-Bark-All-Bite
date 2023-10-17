package com.infamous.all_bark_all_bite.common.compat;

import baguchan.revampedwolf.WolfConfigs;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class RWCompat {

    private static HasCustomInventoryScreen screenCast(Dog dog){
        return (HasCustomInventoryScreen) dog;
    }

    public static Optional<InteractionResult> mobInteract(Dog dog, Player player, InteractionHand hand) {
        if (!WolfConfigs.COMMON.disableWolfArmor.get() && player.isSecondaryUseActive() && dog.isTame() && dog.isOwnedBy(player)) {
            screenCast(dog).openCustomInventoryScreen(player);
            return Optional.of(InteractionResult.sidedSuccess(player.level().isClientSide));
        }
        return Optional.empty();
    }

}
