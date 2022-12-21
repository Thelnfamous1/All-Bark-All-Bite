package com.infamous.call_of_the_wild.data;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class COTWGiftLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
   public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
      biConsumer.accept(COTWBuiltInLootTables.DOG_DIGGING,
              LootTable.lootTable()
                      .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                              .add(LootItem.lootTableItem(Items.RABBIT_HIDE).setWeight(10))
                              .add(LootItem.lootTableItem(Items.RABBIT_FOOT).setWeight(10))
                              .add(LootItem.lootTableItem(Items.CHICKEN).setWeight(10))
                              .add(LootItem.lootTableItem(Items.FEATHER).setWeight(10))
                              .add(LootItem.lootTableItem(Items.MUTTON).setWeight(10))
                              .add(LootItem.lootTableItem(Items.WHITE_WOOL).setWeight(10))
                              .add(LootItem.lootTableItem(Items.BONE).setWeight(10))
                              .add(LootItem.lootTableItem(Items.ARROW).setWeight(10))));
      }
}