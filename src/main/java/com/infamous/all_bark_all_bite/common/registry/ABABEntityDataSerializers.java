package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.entity.EntityVariant;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class ABABEntityDataSerializers {

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, AllBarkAllBite.MODID);

    public static final RegistryObject<EntityDataSerializer<EntityVariant>> DOG_VARIANT = ENTITY_DATA_SERIALIZERS.register("dog_variant", () -> forgeId(ABABDogVariants.DOG_VARIANT_REGISTRY.get()));

    private static <T> EntityDataSerializer<T> forgeId(IForgeRegistry<T> registry) {
        return EntityDataSerializer.simple((fbb, t) -> fbb.writeRegistryId(registry, t), IForgeFriendlyByteBuf::readRegistryId);
    }
}
