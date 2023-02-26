package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.behavior.TargetBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraftforge.common.util.TriPredicate;

import java.util.Optional;
import java.util.function.Predicate;

public class OwnerHurtByTarget<M extends Mob & OwnableEntity> extends TargetBehavior<M> {
    private LivingEntity ownerLastHurtBy;
    private int timestamp;
    private final Predicate<M> canCheck;
    private final TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack;
    private LivingEntity owner;

    public OwnerHurtByTarget(){
        this(mob -> true, (mob, target, owner) -> true);
    }

    public OwnerHurtByTarget(Predicate<M> canCheck, TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack) {
        super(ImmutableMap.of(), false);
        this.canCheck = canCheck;
        this.wantsToAttack = wantsToAttack;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, M tamable) {
        if (this.canCheck.test(tamable)) {
            Optional<LivingEntity> owner = AiUtil.getOwner(tamable);
            if (owner.isEmpty()) {
                return false;
            } else {
                this.owner = owner.get();
                this.ownerLastHurtBy = this.owner.getLastHurtByMob();
                int lastHurtByMobTimestamp = this.owner.getLastHurtByMobTimestamp();
                return lastHurtByMobTimestamp != this.timestamp
                        && this.canAttack(tamable, this.ownerLastHurtBy, TargetingConditions.DEFAULT)
                        && this.wantsToAttack.test(tamable, this.ownerLastHurtBy, this.owner);
            }
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel level, M tamable, long gameTime) {
        StartAttacking.setAttackTarget(tamable, this.ownerLastHurtBy);
        this.timestamp = this.owner.getLastHurtByMobTimestamp();
        super.start(level, tamable, gameTime);
    }
}
