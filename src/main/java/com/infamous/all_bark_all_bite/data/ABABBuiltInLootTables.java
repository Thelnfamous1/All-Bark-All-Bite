package com.infamous.all_bark_all_bite.data;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.resources.ResourceLocation;

public class ABABBuiltInLootTables {
   private static final Set<ResourceLocation> LOCATIONS = Sets.newHashSet();
   private static final Set<ResourceLocation> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);
   public static final ResourceLocation DOG_DIGGING = register("gameplay/dog_digging");
   public static final ResourceLocation KENNEL_ILLAGER_HOUND = register(String.format("chests/kennel/%s", ABABEntityTypes.ILLAGER_HOUND_NAME));
   public static final ResourceLocation KENNEL_HOUNDMASTER = register("chests/kennel/houndmaster");

   @SuppressWarnings("SameParameterValue")
   private static ResourceLocation register(String path) {
      return register(new ResourceLocation(AllBarkAllBite.MODID, path));
   }

   private static ResourceLocation register(ResourceLocation location) {
      if (LOCATIONS.add(location)) {
         return location;
      } else {
         throw new IllegalArgumentException(location + " is already a registered built-in loot table");
      }
   }

   public static Set<ResourceLocation> all() {
      return IMMUTABLE_LOCATIONS;
   }
}