package com.infamous.all_bark_all_bite.common.structure;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import com.infamous.all_bark_all_bite.mixin.StructureTemplatePoolAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = AllBarkAllBite.MODID)
public class StructurePieceAppender {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "empty"));

    /**
     * Adds the structure piece to the targeted pool.
     * We will call this in addStructurePieces method further down to add to every targeted structure template pool.
     * <p>
     * Note: This is an additive operation which means multiple mods can do this, and they stack with each other safely.
     */
    private static void addStructurePieceToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                                Registry<StructureProcessorList> processorListRegistry,
                                                ResourceLocation poolRL,
                                                String nbtPieceRL,
                                                int weight) {

        // Grabs the processor list we want to use along with our piece.
        // This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
        // The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        // Use .legacy( for villages/outposts and .single( for everything else
        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            ((StructureTemplatePoolAccessor)pool).getTemplates().add(piece);
        }

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So let's add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        //   NOTE: This is a com.mojang.datafixers.util.Pair. It is NOT a fastUtil pair class. Use the mojang class.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(((StructureTemplatePoolAccessor)pool).getRawTemplates());
        listOfPieceEntries.add(new Pair<>(piece, weight));
        ((StructureTemplatePoolAccessor)pool).setRawTemplates(listOfPieceEntries);
    }


    /**
     * We use FMLServerAboutToStartEvent as the dynamic registry exists now and all JSON worldgen files were parsed.
     * Mod compat is best done here.
     */
    @SubscribeEvent
    static void addStructurePieces(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registry.PROCESSOR_LIST_REGISTRY).orElseThrow();

        if(ABABConfig.addDogsToVillageCatPool.get()){
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "black");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "blue");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "brown");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "cream");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "gold");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "gray");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "red");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "white");
            addDogToCatPool(templatePoolRegistry, processorListRegistry, "yellow");
        }
        if(ABABConfig.addKennelToOutpostFeaturesPool.get()){
            addKennelToOutpostPool(templatePoolRegistry, processorListRegistry);
        }
    }

    private static void addKennelToOutpostPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry) {
        addPieceToPool(templatePoolRegistry, processorListRegistry,
                "pillager_outpost/feature_kennel",
                new ResourceLocation("minecraft:pillager_outpost/features"), 1);
    }

    private static void addDogToCatPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, String variant) {
        addPieceToPool(templatePoolRegistry, processorListRegistry,
                String.format("%s_%s", "village/common/animals/dog", variant),
                new ResourceLocation("minecraft:village/common/cats"), 1);
    }

    private static void addPieceToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, String path, ResourceLocation poolRL, int weight) {
        String nbtPieceRL = new ResourceLocation(AllBarkAllBite.MODID, path).toString();
        AllBarkAllBite.LOGGER.info("Adding structure piece {} to structure template pool {} with weight {}", nbtPieceRL, poolRL, weight);
        addStructurePieceToPool(templatePoolRegistry, processorListRegistry,
                poolRL,
                nbtPieceRL, weight);
    }
}
