package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.entity.EntityVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class COTWDogVariants {
    public static final DeferredRegister<EntityVariant> DOG_VARIANTS = DeferredRegister.create(new ResourceLocation(CallOfTheWild.MODID, "dog_variant"), CallOfTheWild.MODID);

    public static final Supplier<IForgeRegistry<EntityVariant>> DOG_VARIANT_REGISTRY = DOG_VARIANTS.makeRegistry(RegistryBuilder::new);

    private static final String BROWN_TEXTURE_NAME = "brown";
    public static final RegistryObject<EntityVariant> BROWN = DOG_VARIANTS.register(BROWN_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, BROWN_TEXTURE_NAME));

    private static final String BLACK_TEXTURE_NAME = "black";
    public static final RegistryObject<EntityVariant> BLACK = DOG_VARIANTS.register(BLACK_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, BLACK_TEXTURE_NAME));

    private static final String WHITE_TEXTURE_NAME = "white";
    public static final RegistryObject<EntityVariant> WHITE = DOG_VARIANTS.register(WHITE_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, WHITE_TEXTURE_NAME));

    private static final String GOLD_TEXTURE_NAME = "gold";
    public static final RegistryObject<EntityVariant> GOLD = DOG_VARIANTS.register(GOLD_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, GOLD_TEXTURE_NAME));

    private static final String RED_TEXTURE_NAME = "red";
    public static final RegistryObject<EntityVariant> RED = DOG_VARIANTS.register(RED_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, RED_TEXTURE_NAME));

    private static final String YELLOW_TEXTURE_NAME = "yellow";
    public static final RegistryObject<EntityVariant> YELLOW = DOG_VARIANTS.register(YELLOW_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, YELLOW_TEXTURE_NAME));

    private static final String CREAM_TEXTURE_NAME = "cream";
    public static final RegistryObject<EntityVariant> CREAM = DOG_VARIANTS.register(CREAM_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, CREAM_TEXTURE_NAME));

    private static final String GRAY_TEXTURE_NAME = "gray";
    public static final RegistryObject<EntityVariant> GRAY = DOG_VARIANTS.register(GRAY_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, GRAY_TEXTURE_NAME));

    private static final String BLUE_TEXTURE_NAME = "blue";
    public static final RegistryObject<EntityVariant> BLUE = DOG_VARIANTS.register(BLUE_TEXTURE_NAME,
            () -> new EntityVariant(CallOfTheWild.MODID, COTWEntityTypes.DOG_NAME, BLUE_TEXTURE_NAME));

}
