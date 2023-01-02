package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.entity.dog.ai.Dog;
import com.infamous.call_of_the_wild.common.entity.dog.vibration.DogVibrationListenerConfig;
import com.infamous.call_of_the_wild.common.entity.dog.vibration.WolfVibrationListenerConfig;
import com.infamous.call_of_the_wild.common.sensor.AllySensor;
import com.infamous.call_of_the_wild.common.sensor.AnimalTemptationSensor;
import com.infamous.call_of_the_wild.common.sensor.DogSpecificSensor;
import com.infamous.call_of_the_wild.common.sensor.WolfSpecificSensor;
import com.infamous.call_of_the_wild.common.sensor.vibration.VibrationSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class COTWSensorTypes {

    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, CallOfTheWild.MODID);

    public static final RegistryObject<SensorType<AllySensor>> NEAREST_ALLIES = SENSOR_TYPES.register("nearest_allies",
            () -> new SensorType<>(AllySensor::new));

    public static final RegistryObject<SensorType<DogSpecificSensor>> DOG_SPECIFIC_SENSOR = SENSOR_TYPES.register("dog_specific_sensor",
            () -> new SensorType<>(DogSpecificSensor::new));

    public static final RegistryObject<SensorType<AnimalTemptationSensor>> ANIMAL_TEMPTATIONS = SENSOR_TYPES.register("animal_temptations",
            () -> new SensorType<>(AnimalTemptationSensor::new));

    public static final RegistryObject<SensorType<WolfSpecificSensor>> WOLF_SPECIFIC_SENSOR = SENSOR_TYPES.register("wolf_specific_sensor",
            () -> new SensorType<>(WolfSpecificSensor::new));

    public static final RegistryObject<SensorType<VibrationSensor<Wolf, WolfVibrationListenerConfig>>> WOLF_VIBRATION_SENSOR = SENSOR_TYPES.register("wolf_vibration_sensor",
            () -> new SensorType<>(() -> new VibrationSensor<>(WolfVibrationListenerConfig::new, COTWMemoryModuleTypes.WOLF_VIBRATION_LISTENER.get())));

    @SuppressWarnings("unused")
    public static final RegistryObject<SensorType<VibrationSensor<Dog, DogVibrationListenerConfig>>> DOG_VIBRATION_SENSOR = SENSOR_TYPES.register("dog_vibration_sensor",
            () -> new SensorType<>(() -> new VibrationSensor<>(DogVibrationListenerConfig::new, COTWMemoryModuleTypes.DOG_VIBRATION_LISTENER.get())));
}
