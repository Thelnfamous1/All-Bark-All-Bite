package com.infamous.all_bark_all_bite.common.util;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class DebugUtil {

    @SuppressWarnings("SameParameterValue")
    private static void sendCustomPayloadPacketToAllPlayers(ServerLevel level, FriendlyByteBuf friendlyByteBuf, ResourceLocation location) {
        Packet<?> packet = new ClientboundCustomPayloadPacket(location, friendlyByteBuf);

        for(ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    public static void sendEntityBrain(LivingEntity livingEntity, ServerLevel level, MemoryModuleType<?>... memoriesToCheck) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        writeBrain(livingEntity, byteBuf, memoriesToCheck);
        sendCustomPayloadPacketToAllPlayers(level, byteBuf, ClientboundCustomPayloadPacket.DEBUG_BRAIN);
    }

    private static void writeBrain(LivingEntity livingEntity, FriendlyByteBuf byteBuf, MemoryModuleType<?>... memoriesToCheck) {
        Brain<?> brain = livingEntity.getBrain();
        long gameTime = livingEntity.level.getGameTime();
        writeBrainDumpArguments(livingEntity, byteBuf, brain, gameTime);
        writeBrainDumpAdditional(livingEntity, byteBuf, brain, gameTime, memoriesToCheck);
    }

    private static void writeBrainDumpArguments(LivingEntity livingEntity, FriendlyByteBuf byteBuf, Brain<?> brain, long gameTime) {
        // position gets read first in ClientPacketListener#handleCustomPayload
        writePos(livingEntity, byteBuf);

        byteBuf.writeUUID(livingEntity.getUUID());
        byteBuf.writeInt(livingEntity.getId());
        byteBuf.writeUtf(DebugEntityNameGenerator.getEntityName(livingEntity));
        writeProfession(livingEntity, byteBuf);
        writeXp(livingEntity, byteBuf);
        byteBuf.writeFloat(livingEntity.getHealth());
        byteBuf.writeFloat(livingEntity.getMaxHealth());

        // position is passed in here in constructor for BrainDebugRenderer.BrainDump

        writeInventory(livingEntity, byteBuf);
        writePath(byteBuf, brain);
        writeWantsGolem(livingEntity, byteBuf, gameTime);
        writerAngerLevel(livingEntity, byteBuf);
    }

    private static void writePos(LivingEntity livingEntity, FriendlyByteBuf byteBuf) {
        Vec3 position = livingEntity.position();
        byteBuf.writeDouble(position.x);
        byteBuf.writeDouble(position.y);
        byteBuf.writeDouble(position.z);
    }

    private static void writeProfession(LivingEntity livingEntity, FriendlyByteBuf byteBuf) {
        if(livingEntity instanceof Villager villager){
            byteBuf.writeUtf(villager.getVillagerData().getProfession().name());
        } else{
            byteBuf.writeUtf("");
        }
    }

    private static void writeXp(LivingEntity livingEntity, FriendlyByteBuf byteBuf) {
        if(livingEntity instanceof Villager villager){
            byteBuf.writeInt(villager.getVillagerXp());
        } else{
            byteBuf.writeInt(0);
        }
    }

    private static void writeInventory(LivingEntity livingEntity, FriendlyByteBuf byteBuf) {
        if (livingEntity instanceof InventoryCarrier inventoryCarrier) {
            Container container = inventoryCarrier.getInventory();
            byteBuf.writeUtf(container.isEmpty() ? "" : container.toString());
        } else {
            byteBuf.writeUtf("");
        }
    }

    @SuppressWarnings("unused")
    private static void writePath(FriendlyByteBuf byteBuf, Brain<?> brain) {
        /*
        byteBuf.writeOptional(brain.hasMemoryValue(MemoryModuleType.PATH) ?
                brain.getMemory(MemoryModuleType.PATH) :
                Optional.empty(),
                (fbb, p) -> p.writeToStream(fbb));
         */
        byteBuf.writeBoolean(false); // Path fails to get created from stream properly on packet reception
    }

    private static void writeBrainDumpAdditional(LivingEntity livingEntity, FriendlyByteBuf byteBuf, Brain<?> brain, long gameTime, MemoryModuleType<?>... memoriesToCheck) {
        writeActivities(byteBuf, brain);
        writeBehaviors(byteBuf, brain);
        writeMemories(livingEntity, byteBuf, gameTime, memoriesToCheck);
        writePois(livingEntity, byteBuf, brain);
        writePotentialPois(livingEntity, byteBuf, brain);
        writeGossips(livingEntity, byteBuf);
    }

    private static void writeActivities(FriendlyByteBuf byteBuf, Brain<?> brain) {
        byteBuf.writeCollection(brain.getActiveActivities(), (fbb, a) -> fbb.writeUtf(a.getName()));
    }

    private static void writeBehaviors(FriendlyByteBuf byteBuf, Brain<?> brain) {
        Set<String> runningBehaviors = brain.getRunningBehaviors().stream()
                .map(BehaviorControl::debugString)
                .collect(Collectors.toSet());
        byteBuf.writeCollection(runningBehaviors, FriendlyByteBuf::writeUtf);
    }

    private static void writeMemories(LivingEntity livingEntity, FriendlyByteBuf byteBuf, long gameTime, MemoryModuleType<?>... memoriesToCheck) {
        byteBuf.writeCollection(getMemoryDescriptions(livingEntity, gameTime, memoriesToCheck), (fbb, s) -> {
            String truncatedString = StringUtil.truncateStringIfNecessary(s, 255, true);
            fbb.writeUtf(truncatedString);
        });
    }

    private static void writeGossips(LivingEntity livingEntity, FriendlyByteBuf byteBuf) {
        if (livingEntity instanceof Villager villager) {
            Map<UUID, Object2IntMap<GossipType>> gossipEntries = villager.getGossips().getGossipEntries();
            List<String> gossips = Lists.newArrayList();
            gossipEntries.forEach((uuid, gossipEntry) -> {
                String s = DebugEntityNameGenerator.getEntityName(uuid);
                gossipEntry.forEach((gossipType, integer) -> gossips.add(s + ": " + gossipType + ": " + integer));
            });
            byteBuf.writeCollection(gossips, FriendlyByteBuf::writeUtf);
        } else {
            byteBuf.writeVarInt(0);
        }
    }

    private static void writePotentialPois(LivingEntity livingEntity, FriendlyByteBuf byteBuf, Brain<?> brain) {
        if (livingEntity instanceof Villager) {
            Set<BlockPos> secondaryPositionMemories = Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE)
                    .map(brain::getMemory)
                    .flatMap(Optional::stream)
                    .map(GlobalPos::pos)
                    .collect(Collectors.toSet());
            byteBuf.writeCollection(secondaryPositionMemories, FriendlyByteBuf::writeBlockPos);
        } else {
            byteBuf.writeVarInt(0);
        }
    }

    private static void writePois(LivingEntity livingEntity, FriendlyByteBuf byteBuf, Brain<?> brain) {
        if (livingEntity instanceof Villager) {
            Set<BlockPos> pois = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT)
                    .map(brain::getMemory)
                    .flatMap(Optional::stream)
                    .map(GlobalPos::pos)
                    .collect(Collectors.toSet());
            byteBuf.writeCollection(pois, FriendlyByteBuf::writeBlockPos);
        } else {
            byteBuf.writeVarInt(0);
        }
    }

    private static void writerAngerLevel(LivingEntity livingEntity, FriendlyByteBuf byteBuf) {
        if (livingEntity instanceof Warden warden) {
            byteBuf.writeInt(warden.getClientAngerLevel());
        } else {
            byteBuf.writeInt(-1);
        }
    }

    private static void writeWantsGolem(LivingEntity livingEntity, FriendlyByteBuf byteBuf, long gameTime) {
        if (livingEntity instanceof Villager villager) {
            boolean wantsToSpawnGolem = villager.wantsToSpawnGolem(gameTime);
            byteBuf.writeBoolean(wantsToSpawnGolem);
        } else {
            byteBuf.writeBoolean(false);
        }
    }

    private static List<String> getMemoryDescriptions(LivingEntity livingEntity, long gameTime, MemoryModuleType<?>... memoriesToCheck) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = livingEntity.getBrain().getMemories();
        List<String> memoryDescriptions = Lists.newArrayList();

        if(memoriesToCheck.length > 0){
            for(MemoryModuleType<?> memory : memoriesToCheck){
                Optional<? extends ExpirableValue<?>> memoryValue = memories.getOrDefault(memory, Optional.empty());
                addMemoryDescription(memoryDescriptions, livingEntity, gameTime, memory, memoryValue);
            }
        } else{
            for(Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : memories.entrySet()) {
                MemoryModuleType<?> memory = entry.getKey();
                Optional<? extends ExpirableValue<?>> memoryValue = entry.getValue();
                addMemoryDescription(memoryDescriptions, livingEntity, gameTime, memory, memoryValue);
            }
        }

        memoryDescriptions.sort(String::compareTo);
        return memoryDescriptions;
    }

    private static void addMemoryDescription(List<String> memoryDescriptions, LivingEntity livingEntity, long gameTime, MemoryModuleType<?> memoryModuleType, Optional<? extends ExpirableValue<?>> memoryValue) {
        String memory;
        if (memoryValue.isPresent()) {
            ExpirableValue<?> expirableValue = memoryValue.get();
            Object value = expirableValue.getValue();
            if (value instanceof Long recordedTime) {
                long duration = gameTime - recordedTime;
                memory = duration + " ticks ago";
            } else if (expirableValue.canExpire()) {
                memory = getShortDescription((ServerLevel) livingEntity.level, value) + " (ttl: " + expirableValue.getTimeToLive() + ")";
            } else {
                memory = getShortDescription((ServerLevel) livingEntity.level, value);
            }
        } else {
            memory = "-";
        }

        //noinspection ConstantConditions
        memoryDescriptions.add(ForgeRegistries.MEMORY_MODULE_TYPES.getKey(memoryModuleType).getPath() + ": " + memory);
    }

    private static String getShortDescription(ServerLevel level, @Nullable Object value) {
        if (value == null) {
            return "-";
        } else if (value instanceof UUID) {
            return getShortDescription(level, level.getEntity((UUID)value));
        } else if (value instanceof LivingEntity) {
            Entity entity1 = (Entity)value;
            return DebugEntityNameGenerator.getEntityName(entity1);
        } else if (value instanceof Nameable) {
            return ((Nameable)value).getName().getString();
        } else if (value instanceof WalkTarget) {
            return getShortDescription(level, ((WalkTarget)value).getTarget());
        } else if (value instanceof EntityTracker) {
            return getShortDescription(level, ((EntityTracker)value).getEntity());
        } else if (value instanceof GlobalPos) {
            return getShortDescription(level, ((GlobalPos)value).pos());
        } else if (value instanceof BlockPosTracker) {
            return getShortDescription(level, ((BlockPosTracker)value).currentBlockPosition());
        } else if (value instanceof DamageSource) {
            Entity entity = ((DamageSource)value).getEntity();
            return entity == null ? value.toString() : getShortDescription(level, entity);
        } else if (!(value instanceof Collection<?> collection)) {
            return value.toString();
        } else {
            List<String> list = Lists.newArrayList();

            for(Object listValue : collection) {
                list.add(getShortDescription(level, listValue));
            }

            return list.toString();
        }
    }
}
