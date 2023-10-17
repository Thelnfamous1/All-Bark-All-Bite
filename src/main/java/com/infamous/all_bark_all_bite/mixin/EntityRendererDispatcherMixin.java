package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.client.renderer.ABABWolfRenderer;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRendererDispatcherMixin {
    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow @Final private BlockRenderDispatcher blockRenderDispatcher;
    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;
    @Shadow @Final private EntityModelSet entityModels;
    @Shadow @Final private Font font;
    @Shadow public Map<EntityType<?>, EntityRenderer<?>> renderers;
    @Unique
    private ABABWolfRenderer customWolfRenderer;

    @Inject(method = "getRenderer", at = @At(value = "RETURN"), cancellable = true)
    private void handleGetRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?>> cir){
        if(WolfHooks.canWolfChange(entity.getType(), true, false) && this.customWolfRenderer != null){
            cir.setReturnValue(this.customWolfRenderer);
        }
    }

    @Inject(method = "onResourceManagerReload", at = @At(value = "RETURN"))
    private void handleResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci){
        EntityRendererProvider.Context context = new EntityRendererProvider.Context((EntityRenderDispatcher) (Object)this, this.itemRenderer, this.blockRenderDispatcher, this.itemInHandRenderer, resourceManager, this.entityModels, this.font);
        this.customWolfRenderer = new ABABWolfRenderer(context);
        // Need to call this here in case a mod needs to add a layer to the custom wolf for whatever reason
        // Have to supply a copy of the registered renderers in case null checks aren't accounted for
        //noinspection UnstableApiUsage
        ModLoader.get().postEvent(new EntityRenderersEvent.AddLayers(Util.make(() -> {
            Map<EntityType<?>, EntityRenderer<?>> renderers = new HashMap<>(EntityRenderers.createEntityRenderers(context));
            renderers.put(EntityType.WOLF, this.customWolfRenderer);
            return renderers;
        }), EntityRenderers.createPlayerRenderers(context), context));
    }
}
