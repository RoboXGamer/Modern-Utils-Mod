package net.roboxgamer.modernutils.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.data.KillData;
import net.roboxgamer.modernutils.item.ModCustomDataComponents;
import net.roboxgamer.modernutils.item.ModItems;
import net.minecraft.server.level.ServerLevel;

@EventBusSubscriber(modid = ModernUtilsMod.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide()) {
            // Check if player has kill recorder in inventory
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(ModItems.KILL_RECORDER.get())) {
                    KillData killData = stack.get(ModCustomDataComponents.KILL_DATA.get());
                    if (killData != null && killData.isRecording()) {
                        LivingEntity killedEntity = event.getEntity();
                        // Get XP from killed entity
                        int xpReward = killedEntity.getExperienceReward((ServerLevel)killedEntity.level(), player);
                        // Update kill data with entity name and XP
                        stack.update(
                            ModCustomDataComponents.KILL_DATA.get(),
                            new KillData(killData.kills(), killData.isRecording(), killData.totalXp()),
                            data -> data.addKill(killedEntity.getName().getString(), xpReward)
                        );
                    }
                }
            }
        }
    }
}
