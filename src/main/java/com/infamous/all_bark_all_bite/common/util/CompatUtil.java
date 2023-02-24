package com.infamous.all_bark_all_bite.common.util;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.block.DrumBlock;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.infamous.all_bark_all_bite.common.ai.CommandAi;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.function.Predicate;

public class CompatUtil {
    public static final String REVAMPED_WOLF_MODID = "revampedwolf";
    public static final String DOMESTICATION_INNOVATION_MODID = "domesticationinnovation";
    public static final int DI_WANDER_COMMAND = 0;
    public static final int DI_STAY_COMMAND = 1;
    public static final int DI_FOLLOW_COMMAND = 2;
    private static final String DI_COMMAND_MESSAGE_KEY = "message.domesticationinnovation.command_";
    private static final int DI_DRUM_EFFECT_RADIUS = 32;

    public static boolean isRevampedWolfLoaded() {
       return ModList.get().isLoaded(REVAMPED_WOLF_MODID);
    }

    public static boolean isDILoaded() {
        return ModList.get().isLoaded(DOMESTICATION_INNOVATION_MODID);
    }

    public static boolean isDITrinaryCommandSystemEnabled(){
        return DomesticationMod.CONFIG.trinaryCommandSystem.get();
    }

    public static Optional<Integer> getDICommand(Entity pet){
        if(pet instanceof IComandableMob comandableMob){
            return Optional.of(comandableMob.getCommand());
        }
        return Optional.empty();
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

    public static void handleCommandAiFromDrumCommand(Level level, BlockPos pos, int command, Player issuer){
        Predicate<Entity> tamed = (entity) -> TameableUtils.isTamed(entity) && TameableUtils.getOwnerUUIDOf(entity) != null && TameableUtils.getOwnerUUIDOf(entity).equals(issuer.getUUID());
        AABB area = new AABB(pos.offset(-DI_DRUM_EFFECT_RADIUS, -DI_DRUM_EFFECT_RADIUS, -DI_DRUM_EFFECT_RADIUS), pos.offset(DI_DRUM_EFFECT_RADIUS, DI_DRUM_EFFECT_RADIUS, DI_DRUM_EFFECT_RADIUS));
        for (Animal animal : level.getEntitiesOfClass(Animal.class, area, EntitySelector.NO_SPECTATORS.and(tamed))) {
            switch (command) {
                case DI_WANDER_COMMAND -> CommandAi.commandFree(animal, issuer, false);
                case DI_STAY_COMMAND -> CommandAi.commandSit(animal, issuer, false);
                case DI_FOLLOW_COMMAND -> CommandAi.commandFollow(animal, issuer, false);
            }
        }
    }

    public static boolean hasDIAmphibiousEnchant(Mob mob) {
        return TameableUtils.isTamed(mob) && TameableUtils.hasEnchant(mob, DIEnchantmentRegistry.AMPHIBIOUS);
    }

    public static void handleDIDrum(Player player, Level level, BlockPos blockPos, BlockState blockState) {
        if(blockState.getBlock() instanceof DrumBlock){
            int command = blockState.getValue(DrumBlock.COMMAND);
            handleCommandAiFromDrumCommand(level, blockPos, command, player);
        }
    }
}
