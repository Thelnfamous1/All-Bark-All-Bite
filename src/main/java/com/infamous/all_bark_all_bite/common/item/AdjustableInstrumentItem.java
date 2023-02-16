package com.infamous.all_bark_all_bite.common.item;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.util.InstrumentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class AdjustableInstrumentItem extends InstrumentItem {

    private static final ResourceLocation ADJUSTABLE_INSTRUMENT = new ResourceLocation(AllBarkAllBite.MODID, "adjustable_instrument");
    public static final String SECONDARY_USE_TOOLTIP = String.format("%s.%s", Util.makeDescriptionId("item", ADJUSTABLE_INSTRUMENT), "secondary_use");

    private final TagKey<Instrument> instruments;

    public AdjustableInstrumentItem(Properties properties, TagKey<Instrument> instruments) {
        super(properties, instruments);
        this.instruments = instruments;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        MutableComponent secondaryUseTooltip = Component.translatable(SECONDARY_USE_TOOLTIP);
        components.add(secondaryUseTooltip.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if(player.isSecondaryUseActive()){
            AllBarkAllBite.PROXY.openItemGui(player, itemInHand, hand);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    public Optional<Holder<Instrument>> getInstrument(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            ResourceLocation location = ResourceLocation.tryParse(tag.getString(InstrumentUtil.INSTRUMENT_TAG));
            if (location != null) {
                return InstrumentUtil.getInstrumentHolder(location);
            }
        }

        Iterator<Holder<Instrument>> iterator = getInstruments().iterator();
        return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
    }

    public Iterable<Holder<Instrument>> getInstruments() {
        return Registry.INSTRUMENT.getTagOrEmpty(this.instruments);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> itemStacks) {
        if (this.allowedIn(tab)) {
            for(Holder<Instrument> instrumentHolder : getInstruments()) {
                ItemStack itemStack = create(this, instrumentHolder);
                itemStacks.add(itemStack);
            }
        }
    }
}
