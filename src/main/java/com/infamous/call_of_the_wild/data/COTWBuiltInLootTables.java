package com.infamous.call_of_the_wild.data;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;

import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.resources.ResourceLocation;

public class COTWBuiltInLootTables {
   private static final Set<ResourceLocation> LOCATIONS = Sets.newHashSet();
   private static final Set<ResourceLocation> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);
   public static final ResourceLocation DOG_DIGGING = register("gameplay/dog_digging");

   @SuppressWarnings("SameParameterValue")
   private static ResourceLocation register(String path) {
      return register(new ResourceLocation(CallOfTheWild.MODID, path));
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