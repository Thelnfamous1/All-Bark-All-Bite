package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.behavior.TargetBehavior;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.util.ai.TrustAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.function.Predicate;

public class DefendLikedPlayer<E extends Mob> extends TargetBehavior<E> {
    private LivingEntity trustedLastHurtBy;
    private LivingEntity trustedLastHurt;
    private int timestamp;
    private final Predicate<LivingEntity> selector;

    public DefendLikedPlayer(){
        this(le -> le.getLastHurtMob() != null && le.getLastHurtMobTimestamp() < le.tickCount + 600);
    }

    public DefendLikedPlayer(Predicate<LivingEntity> selector) {
        super(ImmutableMap.of(MemoryModuleType.LIKED_PLAYER, MemoryStatus.VALUE_PRESENT),false);
        this.selector = selector;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        LivingEntity likedPlayer = TrustAi.getLikedPlayer(mob).get();
        this.trustedLastHurt = likedPlayer;
        this.trustedLastHurtBy = likedPlayer.getLastHurtByMob();
        int lastHurtByMobTimestamp = likedPlayer.getLastHurtByMobTimestamp();
        return lastHurtByMobTimestamp != this.timestamp
                && this.canAttack(mob, this.trustedLastHurtBy, this.getTargetingConditions(mob));
    }

    private TargetingConditions getTargetingConditions(E mob) {
        return TargetingConditions.forCombat().range(AiUtil.getFollowRange(mob)).selector(this.selector);
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        GenericAi.setAttackTarget(mob, this.trustedLastHurtBy);
        this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
        super.start(level, mob, gameTime);
      }
}