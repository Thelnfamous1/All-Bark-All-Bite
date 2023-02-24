package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.registry.ABABSoundEvents;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import org.jetbrains.annotations.NotNull;

public class ABABSoundDefinitionsProvider extends SoundDefinitionsProvider {

    private static final ResourceLocation WHISTLE_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, "whistle");
    private static final double SEMITONE_MULTIPLIER = 1.06D;

    public ABABSoundDefinitionsProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, AllBarkAllBite.MODID, helper);
    }

    public static ABABSoundDefinitionsProvider create(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        return new ABABSoundDefinitionsProvider(generator, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        this.add(ABABSoundEvents.ATTACK_WHISTLE.get(), defineWhistle(-3));
        this.add(ABABSoundEvents.COME_WHISTLE.get(), defineWhistle(-2));
        this.add(ABABSoundEvents.FOLLOW_WHISTLE.get(), defineWhistle(-1));
        this.add(ABABSoundEvents.FREE_WHISTLE.get(), defineWhistle(0));
        this.add(ABABSoundEvents.GO_WHISTLE.get(), defineWhistle(1));
        this.add(ABABSoundEvents.HEEL_WHISTLE.get(), defineWhistle(2));
        this.add(ABABSoundEvents.SIT_WHISTLE.get(), defineWhistle(3));
    }

    @NotNull
    private static SoundDefinition defineWhistle(int octaves) {
        return SoundDefinition.definition()
                .with(SoundDefinition.Sound
                        .sound(WHISTLE_LOCATION, SoundDefinition.SoundType.SOUND)
                        .volume(0.5D)
                        .pitch(shiftPitch(octaves)));
    }

    /**
     * There are 12 semitones in an octave, so an increase by a single semitone corresponds to a pitch multiplier of 2^(1/12)
     * or about 1.06 and hence you can use a multiplier of 1.06^N to shift by N semitones.
     * A semitone makes for a natural sounding pitch step for a rising or falling sequence.
     * @see <a href="https://www.gamedeveloper.com/audio/the-power-of-pitch-shifting">Game Developer: The Power of Pitch Shifting</a>
     */
    private static double shiftPitch(int octaves){
        return Math.pow(SEMITONE_MULTIPLIER, octaves);
    }
}
