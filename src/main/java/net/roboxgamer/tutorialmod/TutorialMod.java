package net.roboxgamer.tutorialmod;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.roboxgamer.tutorialmod.block.ModBlocks;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.client.screen.MechanicalCrafterScreen;
import net.roboxgamer.tutorialmod.item.ModCreativeModTabs;
import net.roboxgamer.tutorialmod.item.ModItems;
import net.roboxgamer.tutorialmod.menu.ModMenuTypes;
import net.roboxgamer.tutorialmod.network.ClientPayloadHandler;
import net.roboxgamer.tutorialmod.network.RemainItemTogglePayload;
import net.roboxgamer.tutorialmod.network.ServerPayloadHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TutorialMod.MODID)
public class TutorialMod {
  public static final String MODID = "tutorialmod";
  public static final Logger LOGGER = LogUtils.getLogger();
  
  public static ResourceLocation location(String path){
    return ResourceLocation.fromNamespaceAndPath(MODID, path);
  }
  
  public ResourceLocation location(String namespace, String path){
    return ResourceLocation.fromNamespaceAndPath(namespace, path);
  }

  public TutorialMod(IEventBus modEventBus, ModContainer modContainer) {
    // Register the commonSetup method for modloading
    modEventBus.addListener(this::commonSetup);

    NeoForge.EVENT_BUS.register(this);
    
    //modEventBus.addListener(this::register);

    ModCreativeModTabs.register(modEventBus);

    // Register the items
    ModItems.register(modEventBus);

    // Register the blocks
    ModBlocks.register(modEventBus);

    ModBlockEntities.register(modEventBus);
    ModMenuTypes.register(modEventBus);

    modEventBus.addListener(this::registerCapabilities);
    modEventBus.addListener(this::registerScreens);

    // Register the item to a creative tab
    modEventBus.addListener(this::addCreative);

    // Config setup
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
  }

  private void addCreative(BuildCreativeModeTabContentsEvent event) {
//    if (event.getTabKey() == CreativeModeTabs.SEARCH){
//      ModItems.ITEMS.getEntries().forEach(item -> event.accept(item.get()));
//    }
  }
  
  
  private void registerScreens(RegisterMenuScreensEvent event) {
    event.register(ModMenuTypes.MECHANICAL_CRAFTER_MENU.get(), MechanicalCrafterScreen::new);
  }

  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {

  }

  private void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModBlockEntities.MECHANICAL_CRAFTER_BE.get(),
        (be,side) -> be.getCombinedInvWrapper()
    );
  }


  @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(@NotNull FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
        Minecraft.getInstance().getWindow().setWindowed(1280,720);
      });
    }
    
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
      // Sets the current network version
      final PayloadRegistrar registrar = event.registrar("1");
      registrar.playBidirectional(
          RemainItemTogglePayload.TYPE,
          RemainItemTogglePayload.STREAM_CODEC,
          new DirectionalPayloadHandler<>(
              ClientPayloadHandler::handleData,
              ServerPayloadHandler::handleData
          )
      );
    }
  }
}
