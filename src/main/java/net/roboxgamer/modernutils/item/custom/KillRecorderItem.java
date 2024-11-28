package net.roboxgamer.modernutils.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.TooltipFlag;
import net.roboxgamer.modernutils.data.KillData;
import net.roboxgamer.modernutils.item.ModCustomDataComponents;

import java.util.ArrayList;
import java.util.List;

public class KillRecorderItem extends Item {
    public KillRecorderItem(Properties properties) {
        super(properties.setNoRepair());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            
            if (player.isShiftKeyDown()) {
                DataComponentType<KillData> killDataType = ModCustomDataComponents.KILL_DATA.get();
                KillData currentData = stack.getOrDefault(killDataType, KillData.createEmpty());
                
                if (!currentData.isRecording()) {
                    // Start recording - clear previous data and start fresh
                    long currentTime = System.currentTimeMillis();
                    stack.update(
                        killDataType,
                        currentData,
                        data -> new KillData(new ArrayList<>(), true, 0, currentTime, 0L)
                    );
                    player.displayClientMessage(Component.literal("Kill Recording: Enabled (Data Cleared)"), true);
                } else {
                    // Stop recording
                    long currentTime = System.currentTimeMillis();
                    stack.update(
                        killDataType,
                        currentData,
                        data -> new KillData(data.kills(), false, data.totalXp(), data.recordingStart(), currentTime)
                    );
                    player.displayClientMessage(Component.literal("Kill Recording: Disabled"), true);
                }
            }
        }
        return super.use(level, player, hand);
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        DataComponentType<KillData> killDataType = ModCustomDataComponents.KILL_DATA.get();
        KillData data = stack.getOrDefault(killDataType, KillData.createEmpty());
        
        // Recording Status
        tooltipComponents.add(Component.literal("§6Status: " + (data.isRecording() ? "§aRecording" : "§cNot Recording")));
        
        // Kills Count
        tooltipComponents.add(Component.literal("§6Total Kills: §f" + data.kills().size()));
        
        // Total XP
        tooltipComponents.add(Component.literal("§6Total XP: §f" + data.totalXp()));
        
        // Recording Duration
        if (data.recordingStart() > 0) {
            long duration;
            if (data.isRecording()) {
                duration = System.currentTimeMillis() - data.recordingStart();
            } else if (data.recordingEnd() > 0) {
                duration = data.recordingEnd() - data.recordingStart();
            } else {
                duration = 0;
            }
            
            long seconds = duration / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            tooltipComponents.add(Component.literal(String.format("§6Duration: §f%02d:%02d", minutes, seconds)));
        }
        
        // Instructions
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§7Shift + Right Click to toggle recording"));
    }
}
