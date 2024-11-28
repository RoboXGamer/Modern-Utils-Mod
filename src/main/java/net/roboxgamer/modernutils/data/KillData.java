package net.roboxgamer.modernutils.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public record KillData(List<String> kills, boolean isRecording, int totalXp, long recordingStart, long recordingEnd) {
    private static final IntFunction<List<String>> LIST_FACTORY = ArrayList::new;

    public static final Codec<KillData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(Codec.STRING).fieldOf("kills").forGetter(KillData::kills),
            Codec.BOOL.fieldOf("recording").forGetter(KillData::isRecording),
            Codec.INT.fieldOf("totalXp").forGetter(KillData::totalXp),
            Codec.LONG.fieldOf("recordingStart").forGetter(KillData::recordingStart),
            Codec.LONG.fieldOf("recordingEnd").forGetter(KillData::recordingEnd)
        ).apply(instance, KillData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, KillData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(LIST_FACTORY, ByteBufCodecs.STRING_UTF8), KillData::kills,
            ByteBufCodecs.BOOL, KillData::isRecording,
            ByteBufCodecs.INT, KillData::totalXp,
            ByteBufCodecs.VAR_LONG, KillData::recordingStart,
            ByteBufCodecs.VAR_LONG, KillData::recordingEnd,
            KillData::new);

    public KillData addKill(String entityName, int xp) {
        List<String> newKills = kills();
        newKills.add(entityName);
        return new KillData(newKills, isRecording(), totalXp() + xp, recordingStart(), recordingEnd());
    }

    public static KillData createEmpty() {
        return new KillData(new ArrayList<>(), false, 0, 0L, 0L);
    }
}
