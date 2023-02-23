package com.infamous.all_bark_all_bite.client;

import com.google.common.collect.Sets;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

@SuppressWarnings("SameParameterValue")
public class ABABModelLayers {
    private static final Set<ModelLayerLocation> ALL_MODELS = Sets.newHashSet();
    public static final ModelLayerLocation DOG = register(ABABEntityTypes.DOG_NAME);
    public static final ModelLayerLocation HOUNDMASTER = register(ABABEntityTypes.HOUNDMASTER_NAME);
    public static final ModelLayerLocation ILLAGER_HOUND = register(ABABEntityTypes.ILLAGER_HOUND_NAME);
    public static final ModelLayerLocation WOLF = register("wolf");
    public static final ModelLayerLocation RW_WOLF_ARMOR = register("wolf_armor");

    private static ModelLayerLocation register(String path) {
        return register("main", new ResourceLocation(AllBarkAllBite.MODID, path));
    }

    private static ModelLayerLocation register(String layer, ResourceLocation location) {
        ModelLayerLocation modellayerlocation = createLocation(layer, location);
        if (!ALL_MODELS.add(modellayerlocation)) {
            throw new IllegalStateException("Duplicate registration for " + modellayerlocation);
        } else {
            return modellayerlocation;
        }
    }

    private static ModelLayerLocation createLocation(String layer, ResourceLocation location) {
        return new ModelLayerLocation(location, layer);
    }
}
