package com.infamous.call_of_the_wild.client;

import com.google.common.collect.Sets;
import com.infamous.call_of_the_wild.AllBarkAllBite;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

@SuppressWarnings("SameParameterValue")
public class COTWModelLayers {
    private static final Set<ModelLayerLocation> ALL_MODELS = Sets.newHashSet();
    public static final ModelLayerLocation DOG = register("dog");

    private static ModelLayerLocation register(String path) {
        return register(path, "main");
    }

    private static ModelLayerLocation register(String path, String layer) {
        ModelLayerLocation modellayerlocation = createLocation(path, layer);
        if (!ALL_MODELS.add(modellayerlocation)) {
            throw new IllegalStateException("Duplicate registration for " + modellayerlocation);
        } else {
            return modellayerlocation;
        }
    }

    private static ModelLayerLocation createLocation(String path, String layer) {
        return new ModelLayerLocation(new ResourceLocation(AllBarkAllBite.MODID, path), layer);
    }
}
