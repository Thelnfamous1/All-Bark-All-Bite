package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.sensor.AdultsSensor;
import com.infamous.call_of_the_wild.common.sensor.AnimalTemptationSensor;
import com.infamous.call_of_the_wild.common.sensor.DogSpecificSensor;
import com.infamous.call_of_the_wild.common.sensor.WolfSpecificSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class COTWSensorTypes {

    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, CallOfTheWild.MODID);

    public static final RegistryObject<SensorType<AdultsSensor>> NEAREST_ADULTS = SENSOR_TYPES.register("nearest_adults",
            () -> new SensorType<>(AdultsSensor::new));

    public static final RegistryObject<SensorType<DogSpecificSensor>> DOG_SPECIFIC_SENSOR = SENSOR_TYPES.register("dog_specific_sensor",
            () -> new SensorType<>(DogSpecificSensor::new));

    public static final RegistryObject<SensorType<AnimalTemptationSensor>> ANIMAL_TEMPTATIONS = SENSOR_TYPES.register("animal_temptations",
            () -> new SensorType<>(AnimalTemptationSensor::new));

    public static final RegistryObject<SensorType<WolfSpecificSensor>> WOLF_SPECIFIC_SENSOR = SENSOR_TYPES.register("wolf_specific_sensor",
            () -> new SensorType<>(WolfSpecificSensor::new));
}
