package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.sensor.*;
import com.infamous.all_bark_all_bite.common.sensor.vibration.EntityVibrationSystem;
import com.infamous.all_bark_all_bite.common.vibration.DogVibrationListenerConfig;
import com.infamous.all_bark_all_bite.common.vibration.WolfVibrationListenerConfig;
import com.infamous.all_bark_all_bite.common.sensor.vibration.VibrationSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ABABSensorTypes {

    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, AllBarkAllBite.MODID);

    public static final RegistryObject<SensorType<AllySensor>> NEAREST_ALLIES = SENSOR_TYPES.register("nearest_allies",
            () -> new SensorType<>(AllySensor::new));

    public static final RegistryObject<SensorType<DogSpecificSensor>> DOG_SPECIFIC_SENSOR = SENSOR_TYPES.register("dog_specific_sensor",
            () -> new SensorType<>(DogSpecificSensor::new));

    public static final RegistryObject<SensorType<AnimalTemptationSensor>> ANIMAL_TEMPTATIONS = SENSOR_TYPES.register("animal_temptations",
            () -> new SensorType<>(AnimalTemptationSensor::new));

    public static final RegistryObject<SensorType<WolfSpecificSensor>> WOLF_SPECIFIC_SENSOR = SENSOR_TYPES.register("wolf_specific_sensor",
            () -> new SensorType<>(WolfSpecificSensor::new));

    public static final RegistryObject<SensorType<VibrationSensor<Wolf, WolfVibrationListenerConfig>>> WOLF_VIBRATION_SENSOR = SENSOR_TYPES.register("wolf_vibration_sensor",
            () -> new SensorType<>(() -> new VibrationSensor<>(EntityVibrationSystem::new, WolfVibrationListenerConfig::new, ABABMemoryModuleTypes.WOLF_VIBRATION_LISTENER.get(), SharedWolfAi.DEFAULT_LISTENER_RANGE)));

    @SuppressWarnings("unused")
    public static final RegistryObject<SensorType<VibrationSensor<Dog, DogVibrationListenerConfig>>> DOG_VIBRATION_SENSOR = SENSOR_TYPES.register("dog_vibration_sensor",
            () -> new SensorType<>(() -> new VibrationSensor<>(EntityVibrationSystem::new, DogVibrationListenerConfig::new, ABABMemoryModuleTypes.DOG_VIBRATION_LISTENER.get(), SharedWolfAi.DEFAULT_LISTENER_RANGE)));

}
