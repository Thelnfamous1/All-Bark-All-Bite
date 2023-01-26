package com.infamous.call_of_the_wild.common.behavior.pet;

import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.behavior.TargetBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraftforge.common.util.TriPredicate;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class OwnerHurtByTarget<M extends Mob & OwnableEntity> extends TargetBehavior<M> {
    private LivingEntity ownerLastHurtBy;
    private int timestamp;
    private final Predicate<M> canCheck;
    private final TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack;

    public OwnerHurtByTarget(){
        this(mob -> true, (mob, target, owner) -> true);
    }

    public OwnerHurtByTarget(Predicate<M> canCheck, TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack) {
        super(false);
        this.canCheck = canCheck;
        this.wantsToAttack = wantsToAttack;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, M tamable) {
        if (this.canCheck.test(tamable)) {
            Optional<LivingEntity> maybeOwner = AiUtil.getOwner(tamable);
            if (maybeOwner.isEmpty()) {
                return false;
            } else {
                LivingEntity owner = maybeOwner.get();
                this.ownerLastHurtBy = owner.getLastHurtByMob();
                int lastHurtByMobTimestamp = owner.getLastHurtByMobTimestamp();
                return lastHurtByMobTimestamp != this.timestamp
                        && this.canAttack(tamable, this.ownerLastHurtBy, TargetingConditions.DEFAULT)
                        && this.wantsToAttack.test(tamable, this.ownerLastHurtBy, owner);
            }
        } else {
            return false;
        }
    }

    @Override
    protected void start(ServerLevel level, M tamable, long gameTime) {
        StartAttacking.setAttackTarget(tamable, this.ownerLastHurtBy);
        AiUtil.getOwner(tamable).ifPresent(owner -> this.timestamp = owner.getLastHurtByMobTimestamp());
        super.start(level, tamable, gameTime);
    }
}
