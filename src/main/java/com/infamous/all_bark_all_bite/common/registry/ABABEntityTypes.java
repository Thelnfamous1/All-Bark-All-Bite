package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.entity.illager_hound.IllagerHound;
import com.infamous.all_bark_all_bite.common.entity.houndmaster.Houndmaster;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ABABEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AllBarkAllBite.MODID);

    public static final String DOG_NAME = "dog";
    public static final RegistryObject<EntityType<Dog>> DOG = ENTITY_TYPES.register(
            DOG_NAME,
            () -> EntityType.Builder.of(Dog::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(10)
                    .build(DOG_NAME)
    );
    public static final String ILLAGER_HOUND_NAME = "scavenger";
    public static final RegistryObject<EntityType<IllagerHound>> ILLAGER_HOUND = ENTITY_TYPES.register(
            ILLAGER_HOUND_NAME,
            () -> EntityType.Builder.of(IllagerHound::new, MobCategory.MONSTER)
                    .sized(0.9F, 1.3F)
                    .clientTrackingRange(10)
                    .build(ILLAGER_HOUND_NAME)
    );
    public static final String HOUNDMASTER_NAME = "houndmaster";
    public static final RegistryObject<EntityType<Houndmaster>> HOUNDMASTER = ENTITY_TYPES.register(
            HOUNDMASTER_NAME,
            () -> EntityType.Builder.of(Houndmaster::new, MobCategory.MONSTER)
                    .canSpawnFarFromPlayer()
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(HOUNDMASTER_NAME)
    );
}
