package com.infamous.all_bark_all_bite.common.entity;

import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

public class DogSpawner implements CustomSpawner {
   private static final int TICK_DELAY = 1200;
   private int nextTick;

   @SuppressWarnings("deprecation")
   public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
      if (spawnFriendlies && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
         --this.nextTick;
         if (this.nextTick <= 0) {
            this.nextTick = TICK_DELAY;
            Player player = level.getRandomPlayer();
            if (player != null) {
               RandomSource random = level.random;
               int xOffset = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
               int zOffset = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
               BlockPos targetPos = player.blockPosition().offset(xOffset, 0, zOffset);
               int horizontalOffset = 10;
               if (level.hasChunksAt(targetPos.getX() - horizontalOffset, targetPos.getZ() - horizontalOffset, targetPos.getX() + horizontalOffset, targetPos.getZ() + horizontalOffset)) {
                  if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, targetPos, ABABEntityTypes.DOG.get())) {
                     if (level.isCloseToVillage(targetPos, 2)) {
                        return this.spawnInVillage(level, targetPos);
                     }


                     if (level.structureManager().getStructureWithPieceAt(targetPos, ABABTags.DOGS_SPAWN_IN).isValid()) {
                        return this.spawnInHut(level, targetPos);
                     }
                  }

               }
            }
         }
      }
      return 0;
   }

   private int spawnInVillage(ServerLevel level, BlockPos blockPos) {
      int horizontalBound = 48;
      if (level.getPoiManager().getCountInRange((h) -> h.is(PoiTypes.HOME), blockPos, horizontalBound, PoiManager.Occupancy.IS_OCCUPIED) > 4L) {
         List<Dog> nearbyDogs = level.getEntitiesOfClass(Dog.class, (new AABB(blockPos)).inflate(horizontalBound, 8.0D, horizontalBound));
         if (nearbyDogs.size() < 5) {
            return this.spawnDog(blockPos, level);
         }
      }

      return 0;
   }

   private int spawnInHut(ServerLevel level, BlockPos blockPos) {
      int horizontalBound = 16;
      List<Dog> nearbyDogs = level.getEntitiesOfClass(Dog.class, (new AABB(blockPos)).inflate(horizontalBound, 8.0D, horizontalBound));
      return nearbyDogs.size() < 1 ? this.spawnDog(blockPos, level) : 0;
   }

   private int spawnDog(BlockPos blockPos, ServerLevel level) {
      Dog dog = ABABEntityTypes.DOG.get().create(level);
      if (dog == null) {
         return 0;
      } else {
         dog.moveTo(blockPos, 0.0F, 0.0F); // Fix MC-147659: Some witch huts spawn the incorrect cat
         ForgeEventFactory.onFinalizeSpawn(dog, level, level.getCurrentDifficultyAt(blockPos), MobSpawnType.NATURAL, null, null);
         level.addFreshEntityWithPassengers(dog);
         return 1;
      }
   }
}
