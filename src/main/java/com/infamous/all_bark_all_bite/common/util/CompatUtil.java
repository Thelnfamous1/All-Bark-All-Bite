package com.infamous.all_bark_all_bite.common.util;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public class CompatUtil {
    public static final String REVAMPED_WOLF_MODID = "revampedwolf";
    public static final String DOMESTICATION_INNOVATION_MODID = "domesticationinnovation";
    public static final int DI_WANDER_COMMAND = 0;
    public static final int DI_STAY_COMMAND = 1;
    public static final int DI_FOLLOW_COMMAND = 2;
    private static final String DI_COMMAND_MESSAGE_KEY = "message.domesticationinnovation.command_";

    public static boolean isRevampedWolfLoaded() {
       return ModList.get().isLoaded(REVAMPED_WOLF_MODID);
    }

    public static boolean isDILoaded() {
        return ModList.get().isLoaded(DOMESTICATION_INNOVATION_MODID);
    }

    public static boolean isDITrinaryCommandSystemEnabled(){
        return DomesticationMod.CONFIG.trinaryCommandSystem.get();
    }

    public static int getDICommand(Entity pet){
        if(pet instanceof IComandableMob comandableMob){
            return comandableMob.getCommand();
        }
        return -1;
    }

    public static void setDICommand(Entity pet, LivingEntity user, int command) {
        if(isDITrinaryCommandSystemEnabled()){
            if(pet instanceof IComandableMob comandableMob){
                comandableMob.setCommand(command);
                if(user instanceof Player player) comandableMob.sendCommandMessage(player, command, pet.getName());
            }
            // The Dog does not implement Citadel's interface, but we can still send the client message when the command is set
            else if(pet instanceof Dog dog && user instanceof Player player){
                player.displayClientMessage(Component.translatable(DI_COMMAND_MESSAGE_KEY + command, dog.getName()), true);
            }
        }
    }
}
