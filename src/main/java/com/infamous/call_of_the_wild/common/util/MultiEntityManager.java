package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiEntityManager implements TickableEntityManager {
   private static final int CONVERSION_DELAY = MiscUtil.seconds(2);
   private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, CONVERSION_DELAY);
   private final Set<Entity> entities;
   private final Set<UUID> entityUuids;

   public static Codec<MultiEntityManager> codec() {
      return RecordCodecBuilder
              .create((instance) -> instance.group(
                      ExtraCodecs.UUID.listOf()
                              .fieldOf("uuids")
                              .orElse(Collections.emptyList())
                              .forGetter(MultiEntityManager::createEntityUuids))
                      .apply(instance, MultiEntityManager::new));
   }

   public MultiEntityManager(List<UUID> entityUuids) {
      this.entities = new HashSet<>();
      this.entityUuids = new HashSet<>(entityUuids.size());
      this.entityUuids.addAll(entityUuids);
   }

   private List<UUID> createEntityUuids() {
      return Streams.concat(this.entities.stream().map(Entity::getUUID), this.entityUuids.stream())
              .collect(Collectors.toList());
   }

   @Override
   public void tick(ServerLevel level, Predicate<Entity> isValid, Consumer<Entity> onInvalid) {
      --this.conversionDelay;
      if (this.conversionDelay <= 0) {
         this.convertFromUuids(level);
         this.conversionDelay = CONVERSION_DELAY;
      }

      Iterator<Entity> entityIterator = this.entities.iterator();

      while(entityIterator.hasNext()) {
         Entity entity = entityIterator.next();
         Entity.RemovalReason removalReason = entity.getRemovalReason();
         boolean isRemoved = removalReason != null;
         boolean valid = isValid.test(entity);
         if(!valid || isRemoved){
            if(!valid) onInvalid.accept(entity);
            entityIterator.remove();
            if (isRemoved) {
               switch (removalReason) {
                  case CHANGED_DIMENSION, UNLOADED_TO_CHUNK, UNLOADED_WITH_PLAYER ->
                          this.entityUuids.add(entity.getUUID());
               }
            }
         }
      }
   }

   private void convertFromUuids(ServerLevel level) {
      Iterator<UUID> uuidIterator = this.entityUuids.iterator();

      while(uuidIterator.hasNext()) {
         UUID uuid = uuidIterator.next();
         Entity entity = level.getEntity(uuid);
         if (entity != null) {
            this.entities.add(entity);
            uuidIterator.remove();
         }
      }
   }

   public Stream<Entity> stream(){
      return this.entities.stream();
   }

   public int size(){
      return this.entities.size() + this.entityUuids.size();
   }

   public void add(Entity entity) {
      boolean untracked = !this.entities.contains(entity);
      if (untracked) {
         this.entityUuids.remove(entity.getUUID());
         this.entities.add(entity);
      }
   }

   public void remove(Entity entity) {
      this.entities.remove(entity);
      this.entityUuids.remove(entity.getUUID());
   }

}