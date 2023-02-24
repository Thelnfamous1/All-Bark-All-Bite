package com.infamous.all_bark_all_bite.common.item;

import com.infamous.all_bark_all_bite.common.ai.CommandAi;
import com.infamous.all_bark_all_bite.common.logic.PetManagement;
import com.infamous.all_bark_all_bite.common.logic.entity_manager.MultiEntityManager;
import com.infamous.all_bark_all_bite.common.registry.ABABInstruments;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PetWhistleItem extends AdjustableInstrumentItem{

    private static final String BOUND_TO_TAG = "BoundTo";
    private static final String NAME_TAG = "Name";
    private static final String UUID_TAG = "UUID";

    public PetWhistleItem(Properties properties, TagKey<Instrument> instruments) {
        super(properties, instruments);
    }

    public static boolean interactWithPet(ItemStack stack, Player player, Entity target, InteractionHand hand) {
        CompoundTag existingBoundToTag = stack.getTagElement(BOUND_TO_TAG);
        if (existingBoundToTag != null) return false;

        if (isOwnedBy(target, player)) {
            CompoundTag boundToTag = stack.getOrCreateTagElement(BOUND_TO_TAG);
            boundToTag.putString(NAME_TAG, target.getName().getString());
            boundToTag.putUUID(UUID_TAG, target.getUUID());

            stack.addTagElement(BOUND_TO_TAG, boundToTag);
            player.setItemInHand(hand, stack);
            player.getCooldowns().addCooldown(stack.getItem(), 20);
            return true;
        }
        return false;
    }

    private static boolean isOwnedBy(Entity target, Entity owner) {
        return target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID())
                || target instanceof AbstractHorse horse && owner.getUUID().equals(horse.getOwnerUUID());
    }

    @Nullable
    private static UUID getPetUUID(ItemStack stack){
        CompoundTag petTag = stack.getTagElement(BOUND_TO_TAG);
        if (petTag != null) {
            return petTag.getUUID(UUID_TAG);

        }
        return null;
    }

    public static void onItemUseStart(LivingEntity user, ItemStack useItem, ServerLevel serverLevel) {
        Optional<Holder<Instrument>> instrumentHolder = ABABItems.WHISTLE.get().getInstrument(useItem);
        if(instrumentHolder.isPresent()){
            Instrument instrument = instrumentHolder.get().value();
            MultiEntityManager petManager = PetManagement.getPetManager(user.getLevel().dimension(), user.getUUID());
            UUID petUUID = getPetUUID(useItem);

            if(instrument == ABABInstruments.ATTACK_WHISTLE.get()){
                AiUtil.getTargetedEntity(user, 16)
                        .filter(LivingEntity.class::isInstance)
                        .map(LivingEntity.class::cast)
                        .ifPresent(target -> commandPet(petManager, dog -> CommandAi.commandAttack(dog, target, user), petUUID));
            }
            if(instrument == ABABInstruments.COME_WHISTLE.get()){
                commandPet(petManager, pet -> CommandAi.commandCome(pet, user, serverLevel), petUUID);
            }
            if(instrument == ABABInstruments.FOLLOW_WHISTLE.get()){
                commandPet(petManager, pet -> CommandAi.commandFollow(pet, user), petUUID);
            }
            if(instrument == ABABInstruments.FREE_WHISTLE.get()){
                commandPet(petManager, pet -> CommandAi.commandFree(pet, user), petUUID);
            }
            if(instrument == ABABInstruments.GO_WHISTLE.get()){
                HitResult hitResult = AiUtil.getHitResult(user, 16);
                if(hitResult.getType() != HitResult.Type.MISS){
                    commandPet(petManager, pet -> CommandAi.commandGo(pet, user, hitResult), petUUID);
                }
            }
            if(instrument == ABABInstruments.HEEL_WHISTLE.get()){
                commandPet(petManager, pet -> CommandAi.commandHeel(pet, user), petUUID);
            }
            if(instrument == ABABInstruments.SIT_WHISTLE.get()){
                commandPet(petManager, pet -> CommandAi.commandSit(pet, user), petUUID);
            }
        }
    }

    private static void commandPet(MultiEntityManager petManager, Consumer<PathfinderMob> command, @Nullable UUID petUUID) {
        petManager.stream().forEach(pet -> {
            if(pet instanceof PathfinderMob mob && (petUUID == null || pet.getUUID().equals(petUUID))){
                command.accept(mob);
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        CompoundTag petTag = itemStack.getTagElement(BOUND_TO_TAG);
        if (petTag != null) {
            MutableComponent boundTooltip = Component.translatable(getBoundToTooltipId(), petTag.getString(NAME_TAG));
            components.add(boundTooltip.withStyle(ChatFormatting.GRAY));
        }
    }

    @NotNull
    public static String getBoundToTooltipId() {
        return getWhistleAdditionalInfoTooltipId("bound_to");
    }

    private static String getWhistleAdditionalInfoTooltipId(String additionalInfo) {
        return ABABItems.WHISTLE.get().getDescriptionId() + "." + additionalInfo;
    }
}
