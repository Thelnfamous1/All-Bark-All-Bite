package com.infamous.all_bark_all_bite.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ABABLootTableProvider extends LootTableProvider {
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders =
            ImmutableList.of(
                    Pair.of(ABABGiftLoot::new, LootContextParamSets.GIFT),
                    Pair.of(ABABEntityLoot::new, LootContextParamSets.ENTITY));

    public ABABLootTableProvider(DataGenerator generator) {
        super(generator);
    }

    public static ABABLootTableProvider create(DataGenerator generator){
        return new ABABLootTableProvider(generator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return this.subProviders;
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext) {
        for(ResourceLocation resourcelocation : Sets.difference(ABABBuiltInLootTables.all(), map.keySet())) {
            validationContext.reportProblem("Missing built-in table: " + resourcelocation);
        }

        map.forEach((location, lootTable) -> LootTables.validate(validationContext, location, lootTable));
    }
}
