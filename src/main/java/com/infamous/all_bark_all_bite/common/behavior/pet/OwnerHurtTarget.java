package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.infamous.all_bark_all_bite.common.behavior.TargetBehavior;
import com.infamous.all_bark_all_bite.common.ai.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraftforge.common.util.TriPredicate;

import java.util.Optional;
import java.util.function.Predicate;

public class OwnerHurtTarget<M extends Mob & OwnableEntity> extends TargetBehavior<M> {
    private LivingEntity ownerLastHurt;
    private int timestamp;
    private final Predicate<M> canCheck;
    private final TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack;

    public OwnerHurtTarget(){
        this(mob -> true, (mob, target, owner) -> true);
    }

    public OwnerHurtTarget(Predicate<M> canCheck, TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack) {
        super(false);
        this.canCheck = canCheck;
        this.wantsToAttack = wantsToAttack;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, M tamable) {
        if (this.canCheck.test(tamable)) {
            Optional<LivingEntity> maybeOwner = AiUtil.getOwner(tamable);
            if (maybeOwner.isEmpty()) {
                return false;
            } else {
                LivingEntity owner = maybeOwner.get();
                this.ownerLastHurt = owner.getLastHurtMob();
                int lastHurtMobTimestamp = owner.getLastHurtMobTimestamp();
                return lastHurtMobTimestamp != this.timestamp
                        && this.canAttack(tamable, this.ownerLastHurt, TargetingConditions.DEFAULT)
                        && this.wantsToAttack.test(tamable, this.ownerLastHurt, owner);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start(ServerLevel level, M tamable, long gameTime) {
        StartAttacking.setAttackTarget(tamable, this.ownerLastHurt);
        AiUtil.getOwner(tamable).ifPresent(owner -> this.timestamp = owner.getLastHurtMobTimestamp());
        super.start(level, tamable, gameTime);
    }

}
