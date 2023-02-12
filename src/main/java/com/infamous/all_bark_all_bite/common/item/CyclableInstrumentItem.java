package com.infamous.all_bark_all_bite.common.item;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

public class CyclableInstrumentItem extends InstrumentItem {

    private static final ResourceLocation CYCLABLE_INSTRUMENT = new ResourceLocation(AllBarkAllBite.MODID, "cyclable_instrument");
    public static final String SECONDARY_USE_TOOLTIP = String.format("%s.%s", Util.makeDescriptionId("item", CYCLABLE_INSTRUMENT), "secondary_use");
    protected static final String INSTRUMENT_TAG = "instrument";
    private final TagKey<Instrument> instruments;

    public CyclableInstrumentItem(Properties properties, TagKey<Instrument> instruments) {
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
            Optional<Holder<Instrument>> optional = getInstrument(itemInHand);
            if (optional.isPresent()) {
                cycle(itemInHand, this.instruments, optional.get());
                return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide);
            } else {
                return InteractionResultHolder.fail(itemInHand);
            }
        }
        return super.use(level, player, hand);
    }

    public Optional<Holder<Instrument>> getInstrument(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            ResourceLocation location = ResourceLocation.tryParse(tag.getString(INSTRUMENT_TAG));
            if (location != null) {
                return Registry.INSTRUMENT.getHolder(ResourceKey.create(Registry.INSTRUMENT_REGISTRY, location));
            }
        }

        Iterator<Holder<Instrument>> iterator = Registry.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();
        return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
    }

    protected static void cycle(ItemStack stack, TagKey<Instrument> instruments, Holder<Instrument> original) {
        Optional<Holder<Instrument>> nextInstrument = getNextInstrument(instruments, original);
        nextInstrument.ifPresent(instrumentHolder -> setSoundVariantId(stack, instrumentHolder));
    }

    protected static Optional<Holder<Instrument>> getNextInstrument(TagKey<Instrument> gameEvents, Holder<Instrument> original) {
        return Registry.INSTRUMENT.getTag(gameEvents).flatMap((holderSet) -> {
            if(holderSet.size() == 0) return Optional.empty();
            final Holder<Instrument> firstAvailable = holderSet.get(0);
            if(!holderSet.contains(original)) return Optional.of(firstAvailable);

            Iterator<Holder<Instrument>> itr = holderSet.iterator();
            ResourceLocation originalLocation = getInstrumentLocation(original);
            while (itr.hasNext()) {
                Holder<Instrument> current = itr.next();
                if(current.is(originalLocation)){
                    break;
                }
            }
            return itr.hasNext() ? Optional.of(itr.next()) : Optional.of(firstAvailable);
        });
    }

    protected static void setSoundVariantId(ItemStack itemStack, Holder<Instrument> instrumentHolder) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString(INSTRUMENT_TAG, getInstrumentLocation(instrumentHolder).toString());
    }

    protected static ResourceLocation getInstrumentLocation(Holder<Instrument> instrumentHolder) {
        return instrumentHolder.unwrapKey().orElseThrow(() -> new IllegalStateException("Invalid instrument")).location();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> itemStacks) {
        if (this.allowedIn(tab)) {
            for(Holder<Instrument> instrumentHolder : Registry.INSTRUMENT.getTagOrEmpty(this.instruments)) {
                ItemStack itemStack = create(this, instrumentHolder);
                itemStacks.add(itemStack);
            }
        }
    }
}
