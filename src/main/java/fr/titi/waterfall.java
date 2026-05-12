package fr.titi;

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
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(waterfall.MODID)
public class waterfall {
    public static final String MODID = "waterfall";
    private static final Logger LOGGER = LogUtils.getLogger();

    static ModContainer MOD_CONTAINER;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredBlock<WaterVaporBlock> WATER_VAPOR_BLOCK = BLOCKS.register("water_vapor", WaterVaporBlock::new);
    public static final DeferredBlock<LavaVaporBlock> LAVA_VAPOR_BLOCK = BLOCKS.register("lava_vapor", LavaVaporBlock::new);

    public static Logger getLogger() {
        return LOGGER;
    }

    public waterfall(IEventBus modEventBus, ModContainer modContainer) {
        MOD_CONTAINER = modContainer;

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(Config::onLoad);

        BLOCKS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Waterfall mod initialized - water limit: {}, lava limit: {}",
            Config.maxWaterfallHeight, Config.maxLavafallHeight);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Waterfall mod loaded on server - water limit: {}, lava limit: {}, effects: {}",
            Config.maxWaterfallHeight, Config.maxLavafallHeight, Config.enableEvaporationEffect);
    }

    // Loaded only on the physical client — safe to reference client-only classes here
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MOD_CONTAINER.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
            LOGGER.info("Waterfall mod client setup complete");
        }
    }
}
