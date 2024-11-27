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
import net.roboxgamer.modernutils.data.KillData;
import net.roboxgamer.modernutils.item.ModCustomDataComponents;

import java.util.ArrayList;

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
                KillData currentData = stack.getOrDefault(killDataType, new KillData(new ArrayList<>(), false, 0));
                
                // Toggle recording state
                KillData newData = new KillData(currentData.kills(), !currentData.isRecording(), currentData.totalXp());
                stack.set(killDataType, newData);

                player.displayClientMessage(Component.literal("Kill Recording: " + 
                    (newData.isRecording() ? "Enabled" : "Disabled")), true);
            }
        }
        return super.use(level, player, hand);
    }
}
