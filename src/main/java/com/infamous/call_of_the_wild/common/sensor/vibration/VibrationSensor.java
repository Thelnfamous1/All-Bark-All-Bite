package com.infamous.call_of_the_wild.common.sensor.vibration;

import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.util.BrainUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.function.BiConsumer;

@SuppressWarnings("NullableProblems")
public class VibrationSensor<E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> extends Sensor<E> {
    protected final EntityVibrationListenerConfig.Constructor<E, VLC> listenerConfigFactory;
    protected final MemoryModuleType<EntityVibrationListener<E, VLC>> listenerMemory;
    private DynamicGameEventListener<EntityVibrationListener<E, VLC>> dynamicVibrationListener;

    public VibrationSensor(EntityVibrationListenerConfig.Constructor<E, VLC> listenerConfigFactory, MemoryModuleType<EntityVibrationListener<E, VLC>> listenerMemory) {
        super(1); // needs to tick every second like an entity, this is fine because we are not doing list searches every tick
        this.listenerConfigFactory = listenerConfigFactory;
        this.listenerMemory = listenerMemory;
    }

    @Override
    protected void doTick(ServerLevel level, E mob) {
        this.getDynamicVibrationListener(mob).getListener().tick(level);
    }

    /**
     * This method is used to retrieve this sensor's DynamicGameEventListener instance with the entity as a part of its state
     */
    protected DynamicGameEventListener<EntityVibrationListener<E, VLC>> getDynamicVibrationListener(E mob) {
        if(this.dynamicVibrationListener == null){
            EntityVibrationListener<E, VLC> listener = this.getListener(mob);
            this.dynamicVibrationListener = new DynamicGameEventListener<>(listener);
        }
        return this.dynamicVibrationListener;
    }

    @SuppressWarnings("unchecked")
    private DynamicGameEventListener<?> getDynamicVibrationListenerUnchecked(LivingEntity le) {
        return this.getDynamicVibrationListener((E) le);
    }

    /**
     * This method is used to retrieve a EntityVibrationListener instance with the entity as a part of its state
     */
    protected EntityVibrationListener<E, VLC> getListener(E mob) {
        Brain<?> brain = mob.getBrain();
        EntityVibrationListener<E, VLC> listener = brain.getMemory(this.listenerMemory).orElseGet(() -> this.createDefaultListener(mob));
        listener.getConfig().setEntity(mob);
        brain.setMemory(this.listenerMemory, listener);
        return listener;
    }

    /**
     * This method is used to create an EntityVibrationListener instance with the entity as a part of its state
     */
    protected EntityVibrationListener<E, VLC> createDefaultListener(E mob) {
        PositionSource positionSource = new EntityPositionSource(mob, mob.getEyeHeight());
        VLC listenerConfig = this.listenerConfigFactory.create();
        return new EntityVibrationListener<>(positionSource, this.defaultListenerRange(), listenerConfig, null, 0.0F, 0);
    }

    protected int defaultListenerRange() {
        return 64;
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(this.listenerMemory);
    }

    /**
     * In vanilla, DynamicGameEventListeners associated with an entity's state are updated whenever the entity joins a new ServerLevel, changes its SectionPos in a ServerLevel, or leaves its ServerLevel.
     * Here, we recreate that functionality using Events.
     */
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = CallOfTheWild.MODID)
    private static class EventHandler{

        @SubscribeEvent
        static void onEntityJoinLevel(EntityJoinLevelEvent event){
            if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;
            updateVibrationListener(event.getEntity(), serverLevel, DynamicGameEventListener::add);
        }

        private static void updateVibrationListener(Entity entity, ServerLevel serverLevel, BiConsumer<DynamicGameEventListener<?>, ServerLevel> callback) {
            if(entity instanceof LivingEntity le){
                Brain<?> brain = le.getBrain();
                BrainUtil.getSensors(brain).values().forEach(sensor -> {
                    if(sensor instanceof VibrationSensor<?,?> vibrationSensor){
                        callback.accept(vibrationSensor.getDynamicVibrationListenerUnchecked(le), serverLevel);
                    }
                });
            }
        }

        @SubscribeEvent
        static void onEntitySectionChange(EntityEvent.EnteringSection event){
            if(!(event.getEntity().getLevel() instanceof ServerLevel serverLevel)) return;
            updateVibrationListener(event.getEntity(), serverLevel, DynamicGameEventListener::move);
        }

        @SubscribeEvent
        static void onEntityLeaveLevel(EntityLeaveLevelEvent event){
            if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;
            updateVibrationListener(event.getEntity(), serverLevel, DynamicGameEventListener::remove);
        }
    }
}
