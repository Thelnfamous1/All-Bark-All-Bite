package com.infamous.all_bark_all_bite.common.compat;

import com.alexander.mutantmore.entities.Rodling;
import net.minecraft.world.entity.TamableAnimal;

public class MMCompat {

    public static void setRodlingSitting(TamableAnimal tamableAnimal, boolean wantsToSit){
        if(tamableAnimal instanceof Rodling rodling){
            rodling.setWantsToSit(wantsToSit);
        }
    }
}
