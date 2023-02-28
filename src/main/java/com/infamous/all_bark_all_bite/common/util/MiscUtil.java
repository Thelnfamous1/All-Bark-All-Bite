package com.infamous.all_bark_all_bite.common.util;

import com.google.common.collect.Iterables;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public class MiscUtil {

    public static <T> T getRandomObject(Collection<T> from, RandomSource randomSource) {
        int index = randomSource.nextInt(from.size());
        return Iterables.get(from, index);
    }

    public static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        ItemStack singleton = stack.split(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }

        return singleton;
    }

    @SuppressWarnings("unused")
    public static void addParticlesAroundSelf(LivingEntity livingEntity, ParticleOptions particleOptions, int numParticles, double speedMultiplier, double widthScale, double yOffset) {
        for(int i = 0; i < numParticles; ++i) {
            RandomSource random = livingEntity.getRandom();
            double xSpeed = random.nextGaussian() * speedMultiplier;
            double ySpeed = random.nextGaussian() * speedMultiplier;
            double zSpeed = random.nextGaussian() * speedMultiplier;
            livingEntity.level.addParticle(particleOptions,
                    livingEntity.getRandomX(widthScale),
                    livingEntity.getRandomY() + yOffset,
                    livingEntity.getRandomZ(widthScale),
                    xSpeed,
                    ySpeed,
                    zSpeed);
        }
    }

    public static void addParticlesAroundSelf(LivingEntity livingEntity, ParticleOptions particleOptions, int numParticles, double xSpeed, double ySpeed, double zSpeed, double widthScale, double yOffset) {
        for(int i = 0; i < numParticles; ++i) {
            livingEntity.level.addParticle(particleOptions,
                    livingEntity.getRandomX(widthScale),
                    livingEntity.getRandomY() + yOffset,
                    livingEntity.getRandomZ(widthScale),
                    xSpeed,
                    ySpeed,
                    zSpeed);
        }
    }

    public static void sendParticlesAroundSelf(ServerLevel serverLevel, LivingEntity livingEntity, ParticleOptions particleOptions, double yOffset, int numParticles, double speedMultiplier) {
        serverLevel.sendParticles(particleOptions, livingEntity.getX(), livingEntity.getY() + yOffset, livingEntity.getZ(), numParticles, 0.0D, 0.0D, 0.0D, speedMultiplier);
    }

    public static boolean oneInChance(RandomSource randomSource, int chance) {
        return randomSource.nextInt(chance) == 0;
    }

    public static <T extends Entity> Optional<T> createEntity(@NotNull EntityType<T> entityType, ServerLevel level) {
        T entity = entityType.create(level);

        if(entity == null){
            AllBarkAllBite.LOGGER.warn("Unable to create a new {} in level {}!", EntityType.getKey(entityType), level);
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    public static int seconds(double seconds){
        return (int) (seconds * 20);
    }

    public static void tellServer(ServerLevel serverLevel, Runnable runnable) {
        MinecraftServer server = serverLevel.getServer();
        server.tell(new TickTask(server.getTickCount(), runnable));
    }

    public static String getPngPath(String path) {
        return String.format("%s.png", path);
    }

    public static String getEntityTexturePath(String entityPath, String texturePath) {
        return String.format("textures/entity/%s", getPngPath(String.format("%s/%s", entityPath, texturePath)));
    }
}
