package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.registry.ABABEntityTypes;
import com.infamous.call_of_the_wild.common.registry.ABABItems;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class ABABEntityLoot extends EntityLoot {

    @Override
    protected void addTables() {
        this.add(ABABEntityTypes.DOG.get(), LootTable.lootTable());
        this.add(ABABEntityTypes.ILLAGER_HOUND.get(), LootTable.lootTable());
        this.add(ABABEntityTypes.HOUNDMASTER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ABABItems.WHISTLE.get()))
                        .apply(SetInstrumentFunction.setInstrumentOptions(ABABTags.WHISTLES)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.EMERALD)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                                .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())));
    }

    @Override
    protected Iterable<EntityType<?>> getKnownEntities() {
        return ABABEntityTypes.ENTITY_TYPES.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList());
    }
}
