package com.infamous.all_bark_all_bite.data;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

public class ABABLootTableProvider extends LootTableProvider {
    private static final List<LootTableProvider.SubProviderEntry> TABLES =
            ImmutableList.of(
                    new LootTableProvider.SubProviderEntry(ABABGiftLoot::new, LootContextParamSets.GIFT),
                    new LootTableProvider.SubProviderEntry(ABABEntityLoot::new, LootContextParamSets.ENTITY),
                    new LootTableProvider.SubProviderEntry(ABABChestLoot::new, LootContextParamSets.CHEST));

    public ABABLootTableProvider(DataGenerator generator) {
        super(generator.getPackOutput(), ABABBuiltInLootTables.all(), TABLES);
    }

    public static ABABLootTableProvider create(DataGenerator generator){
        return new ABABLootTableProvider(generator);
    }
}
