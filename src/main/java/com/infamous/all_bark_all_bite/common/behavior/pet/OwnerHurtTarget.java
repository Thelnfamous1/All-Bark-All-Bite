package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.behavior.TargetBehavior;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraftforge.common.util.TriPredicate;

import java.util.Optional;
import java.util.function.Predicate;

public class OwnerHurtTarget<M extends Mob & OwnableEntity> extends TargetBehavior<M> {
    private LivingEntity ownerLastHurt;
    private int timestamp;
    private final Predicate<M> canCheck;
    private final TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack;
    private LivingEntity owner;

    public OwnerHurtTarget(){
        this(mob -> true, (mob, target, owner) -> true);
    }

    public OwnerHurtTarget(TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack) {
        this(mob -> true, wantsToAttack);
    }

    public OwnerHurtTarget(Predicate<M> canCheck, TriPredicate<M, LivingEntity, LivingEntity> wantsToAttack) {
        super(ImmutableMap.of(),false);
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
                this.owner = maybeOwner.get();
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
        GenericAi.setAttackTarget(tamable, this.ownerLastHurt);
        this.timestamp = this.owner.getLastHurtMobTimestamp();
        super.start(level, tamable, gameTime);
    }

}
