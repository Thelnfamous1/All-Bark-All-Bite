package com.infamous.all_bark_all_bite.data;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ABABGiftLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
   public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
      biConsumer.accept(ABABBuiltInLootTables.DOG_DIGGING,
              LootTable.lootTable()
                      .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                              .add(LootItem.lootTableItem(Items.RABBIT).setWeight(10))
                              .add(LootItem.lootTableItem(Items.RABBIT_HIDE).setWeight(10))
                              .add(LootItem.lootTableItem(Items.RABBIT_FOOT).setWeight(10))
                              .add(LootItem.lootTableItem(Items.CHICKEN).setWeight(10))
                              .add(LootItem.lootTableItem(Items.FEATHER).setWeight(10))
                              .add(LootItem.lootTableItem(Items.LEATHER).setWeight(10))
                              .add(LootItem.lootTableItem(Items.GOAT_HORN)
                                      .apply(SetInstrumentFunction.setInstrumentOptions(InstrumentTags.REGULAR_GOAT_HORNS))
                                      .setWeight(2))));
      }
}