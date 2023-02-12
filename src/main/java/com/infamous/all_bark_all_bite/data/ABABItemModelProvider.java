package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ABABItemModelProvider extends ItemModelProvider {
    public ABABItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABItemModelProvider create(DataGenerator generator, ExistingFileHelper existingFileHelper){
        return new ABABItemModelProvider(generator, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.spawnEgg(ABABItems.DOG_SPAWN_EGG);
        this.spawnEgg(ABABItems.ILLAGER_HOUND_SPAWN_EGG);
        this.spawnEgg(ABABItems.HOUNDMASTER_SPAWN_EGG);

        this.basicItem(ABABItems.WHISTLE.get());
    }

    private void spawnEgg(RegistryObject<Item> item) {
        this.withExistingParent(item.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }
}
