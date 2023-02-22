package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ABABChestLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
   public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
      biConsumer.accept(
              ABABBuiltInLootTables.KENNEL_HOUNDMASTER,
              LootTable.lootTable()
                      .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(0.0F, 1.0F))
                              .add(LootItem.lootTableItem(Items.BOW)))
                      .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2.0F, 3.0F))
                              .add(LootItem.lootTableItem(Items.WHEAT).setWeight(7).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 5.0F))))
                              .add(LootItem.lootTableItem(Items.POTATO).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                              .add(LootItem.lootTableItem(Items.CARROT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 5.0F)))))
                      .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 3.0F))
                              .add(LootItem.lootTableItem(Blocks.DARK_OAK_LOG).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))))
                      .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2.0F, 3.0F))
                              .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(7))
                              .add(LootItem.lootTableItem(Items.STRING).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 6.0F))))
                              .add(LootItem.lootTableItem(Items.ARROW).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 7.0F))))
                              .add(LootItem.lootTableItem(Items.LEAD).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                              .add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                              .add(LootItem.lootTableItem(Items.BOOK).setWeight(1).apply(EnchantRandomlyFunction.randomApplicableEnchantment())))
                      .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(0.0F, 1.0F))
                              .add(LootItem.lootTableItem(ABABItems.WHISTLE.get())).apply(SetInstrumentFunction.setInstrumentOptions(ABABTags.WHISTLES))));
      biConsumer.accept(
              ABABBuiltInLootTables.KENNEL_ILLAGER_HOUND,
              LootTable.lootTable()
                      .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 6.0F))
                              .add(LootItem.lootTableItem(Items.RABBIT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                              .add(LootItem.lootTableItem(Items.PORKCHOP).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                              .add(LootItem.lootTableItem(Items.CHICKEN).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                              .add(LootItem.lootTableItem(Items.BEEF).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                              .add(LootItem.lootTableItem(Items.MUTTON).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                              .add(LootItem.lootTableItem(Items.BONE).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))));

   }
}