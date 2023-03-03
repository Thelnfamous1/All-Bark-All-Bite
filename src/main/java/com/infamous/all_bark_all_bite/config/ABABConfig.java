package com.infamous.all_bark_all_bite.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.function.Consumer;

public class ABABConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        COMMON_SPEC = createConfig(ABABConfig::setupCommonConfig);
        CLIENT_SPEC = createConfig(ABABConfig::setupClientConfig);
        SERVER_SPEC = createConfig(ABABConfig::setupServerConfig);
    }

    private static ForgeConfigSpec createConfig(Consumer<ForgeConfigSpec.Builder> setup) {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setup.accept(configBuilder);
        return configBuilder.build();
    }

    public static ForgeConfigSpec.IntValue alertableMaxXZDistance;
    public static ForgeConfigSpec.IntValue alertableMaxYDistance;
    public static ForgeConfigSpec.IntValue dogDigMaxXZDistance;
    public static ForgeConfigSpec.IntValue dogDigMaxYDistance;
    public static ForgeConfigSpec.IntValue dogTargetDetectionDistance;
    public static ForgeConfigSpec.IntValue houndmasterSummonCooldown;
    public static ForgeConfigSpec.IntValue petTeleportDistanceTrigger;
    public static ForgeConfigSpec.IntValue whistleAttackMaxDistance;
    public static ForgeConfigSpec.IntValue whistleGoMaxDistance;
    public static ForgeConfigSpec.IntValue wolfMaxTrust;
    public static ForgeConfigSpec.DoubleValue wolfHitboxSizeScale;
    public static ForgeConfigSpec.IntValue wolfStartingTrust;
    public static ForgeConfigSpec.IntValue wolfTargetDetectionDistance;
    public static ForgeConfigSpec.IntValue wolfTrustIncrement;
    public static ForgeConfigSpec.IntValue wolfTrustDecrement;

    private static void setupCommonConfig(ForgeConfigSpec.Builder builder) {
        createConfigCategory(builder, " This category holds configs that uses numbers.", "Numeric Config Options", b -> {
            alertableMaxXZDistance = b
                    .comment("Determines the maximum horizontal distance away, in blocks, that entities can be to potentially alert Dogs and Wolves.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("alertable_max_xz_distance", 12, 0, 1024);
            alertableMaxYDistance = b
                    .comment("Determines the maximum vertical distance away, in blocks, that entities can be to potentially alert Dogs and Wolves.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("alertable_max_y_distance", 6, 0, 1024);

            dogDigMaxXZDistance = b
                    .comment("Determines the maximum horizontal distance away, in blocks, that Dogs can dig something up at.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("dog_dig_max_xz_distance", 10, 0, 1024);
            dogDigMaxYDistance = b
                    .comment("Determines the maximum vertical distance away, in blocks, that Dogs can dig something up at.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("dog_dig_max_y_distance", 7, 0, 1024);

            dogTargetDetectionDistance = b
                    .comment("Determines the maximum distance away, in blocks, that Dogs can detect potential attack targets.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("dog_target_detection_distance", 16, 0, 16);

            houndmasterSummonCooldown = b
                    .comment("Determines the minimum number of seconds it takes for the Houndmaster to attempt to summon new Scavengers after losing any of them.\n" +
                            "Setting this to -1 effectively disables the feature.")
                    .defineInRange("houndmaster_summon_cooldown", 30, -1, 1200);

            petTeleportDistanceTrigger = b
                    .comment("""
                            The minimum distance away a following pet must be before it will teleport to you instead of pathfinding.
                            Setting this to 0 means the pets will always teleport.
                            Note: Only used for the "Come" whistle command and the AI used by Dogs and Wolves.""")
                    .defineInRange("pet_teleport_distance_trigger", 12, 0, 1024);

            whistleAttackMaxDistance = b
                    .comment("Determines the maximum distance away, in blocks, that the user can target an entity with the \"Attack\" whistle command.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("whistle_attack_max_distance", 16, 0, 1024);
            whistleGoMaxDistance = b
                    .comment("Determines the maximum distance away, in blocks, that the user can target an entity or a block with the \"Go\" whistle command.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("whistle_go_max_distance", 16, 0, 1024);
            wolfMaxTrust = b
                    .comment("Determines the maximum trust level required to tame a trusting wolf.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("wolf_max_trust", 100, 0, 1024);
            wolfHitboxSizeScale = b
                    .comment("Determines the amount the base hitbox size of a Wolf is scaled by during gameplay.\n" +
                            "Setting this to 1.0 leaves it unchanged.")
                    .defineInRange("wolf_hitbox_size_scale", 1.25D, 1.0D, 1024.0D);
            wolfStartingTrust = b
                    .comment("Determines the starting trust level given to bred Wolf pups.")
                    .defineInRange("wolf_starting_trust", 0, -1024, 1024);
            wolfTargetDetectionDistance = b
                    .comment("Determines the maximum distance away, in blocks, that Dogs can detect potential attack targets.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("wolf_target_detection_distance", 16, 0, 16);
            wolfTrustDecrement = b
                    .comment("Determines how much the trust level of a trusting Wolf can decrease by after hurting them.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("wolf_trust_decrement", 5, 0, 1024);
            wolfTrustIncrement = b
                    .comment("Determines how much the trust level of a trusting Wolf can increase by after being given a wolf-liked item.\n" +
                            "Setting this to 0 effectively disables the feature.")
                    .defineInRange("wolf_trust_increment", 5, 0, 1024);
        });
    }

    public static ForgeConfigSpec.DoubleValue wolfRenderSizeScale;

    private static void setupClientConfig(ForgeConfigSpec.Builder builder) {
        createConfigCategory(builder, " This category holds configs that uses numbers.", "Numeric Config Options", b -> {
            wolfRenderSizeScale = b
                    .comment("""
                            Determines the amount the base model size of the Wolf is scaled by when rendering.
                            Setting this to 1.0 leaves it unchanged.
                            Note: This value will be multiplied by the server-side "wolf_hitbox_size_scale" setting for logical consistency.""")
                    .defineInRange("wolf_render_size_scale", 1.0D, 1.0D, 1024.0D);
        });
    }
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> houndmasterRaidWaveCounts;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> dogTemplatePoolTargets;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> kennelTemplatePoolTargets;

    private static void setupServerConfig(ForgeConfigSpec.Builder builder) {
        createConfigCategory(builder, " This category holds configs that uses lists.", "List Config Options", b -> {
            houndmasterRaidWaveCounts = b
                    .comment("""
                            Determines the minimum number of Houndmasters that can spawn in each raid wave.
                            This is meant to be a list of seven integers.
                            A list of less than seven will fill remaining spots with zeroes, and values past the seventh will be ignored.""")
                    .defineList("houndmaster_raid_wave_counts", List.of(0, 0, 0, 0, 1, 1, 2), waveCount -> waveCount instanceof Integer);
            dogTemplatePoolTargets = b
                    .comment("""
                            Determines which structure template pools will have the dog structures located under "all_bark_all_bite:village/common/animals" injected into them.
                            Making this empty prevents the dog structures from being injected into any structure template pools.""")
                    .defineListAllowEmpty(List.of("dog_template_pool_targets"), () ->
                            List.of(new ResourceLocation("minecraft:village/common/cats").toString()), waveCount -> waveCount instanceof String);
            kennelTemplatePoolTargets = b
                    .comment("""
                            Determines which structure template pools will have the kennel structure located under "all_bark_all_bite:pillager_outpost/feature_kennel" injected into them.
                            Making this empty prevents the kennel structure from being injected into any structure template pools.""")
                    .defineListAllowEmpty(List.of("kennel_template_pool_targets"), () ->
                            List.of(new ResourceLocation("minecraft:pillager_outpost/features").toString()), waveCount -> waveCount instanceof String);

        });
    }

    private static void createConfigCategory(ForgeConfigSpec.Builder builder, String comment, String path, Consumer<ForgeConfigSpec.Builder> definitions) {
        builder.comment(comment).push(path);
        definitions.accept(builder);
        builder.pop();
    }
}
