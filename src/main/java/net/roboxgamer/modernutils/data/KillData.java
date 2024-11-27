package net.roboxgamer.modernutils.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public record KillData(List<String> kills, boolean isRecording, int totalXp) {
    private static final IntFunction<List<String>> LIST_FACTORY = ArrayList::new;

    public static final Codec<KillData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(Codec.STRING).fieldOf("kills").forGetter(KillData::kills),
            Codec.BOOL.fieldOf("recording").forGetter(KillData::isRecording),
            Codec.INT.fieldOf("totalXp").forGetter(KillData::totalXp)
        ).apply(instance, KillData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, KillData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(LIST_FACTORY, ByteBufCodecs.STRING_UTF8), KillData::kills,
            ByteBufCodecs.BOOL, KillData::isRecording,
            ByteBufCodecs.INT, KillData::totalXp,
            KillData::new);

    public KillData addKill(String entityName, int xp) {
        List<String> newKills = kills();
        newKills.add(entityName);
        return new KillData(newKills, isRecording(), totalXp() + xp);
    }
}
