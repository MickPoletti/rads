package com.mickpoletti.rads;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Rads.MODID)
public class Rads {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "rads";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under
    // the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under
    // the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be
    // registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final String RADS_FILE_STRING = "rads.json";
    public static final String saveDirectory = System.getProperty("user.dir") + RADS_FILE_STRING;
    // Creates a new Block with the id "examplemod:example_block", combining the
    // namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the
    // namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block",
            EXAMPLE_BLOCK);

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and
    // saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
            new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "examplemod:example_tab" for the example
    // item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS
            .register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rads")) // The language key for the title of your
                                                                     // CreativeModeTab
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this
                                                           // method is preferred over the event
                    }).build());

    public static BlockPos startingPos;
    public static BlockPos endingPos;
    public static boolean firstClick = true;
    private static List<RadiationArea> radiationAreas = new ArrayList<>();
    private static RadiationAreaManager radiationAreaManager = new RadiationAreaManager();
    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public Rads(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod)
        // to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in
        // this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static List<RadiationArea> getRadiationAreas() {
        return Rads.radiationAreas;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("Loading Rads resources...");
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) throws FileNotFoundException {
        // Do something when the server starts
        LOGGER.info("Loading Radiation Areas...");
        /* TODO: Add some configuration checks. If this is the first time the server is starting ever
         * perhaps generate the areas if the user has that setting in place. If not then load from the file.
         * Also check how this behaves if there is no file there. '
        */ 
        for (RadiationArea radiationArea : Rads.radiationAreaManager.loadRadiationAreasFromFile(saveDirectory)) {
            Rads.radiationAreas.add(radiationArea);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Saving Radiation Areas...");
        Rads.radiationAreaManager.save(Rads.radiationAreas, saveDirectory);
    }

    // TODO: check if the user is an admin/if they're holding a specific item
    @SubscribeEvent
    public void onRightClick(RightClickBlock event) {
        if (Rads.firstClick && event.getLevel().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
            Rads.startingPos = event.getPos();
            Rads.firstClick = false;
        } else if (event.getLevel().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
            Rads.endingPos = event.getPos();
            Rads.firstClick = true;
            Rads.radiationAreas.add(new RadiationArea(new AABB(startingPos.getX(), startingPos.getY()-5,
                    startingPos.getZ(), endingPos.getX(), endingPos.getY() + 5, endingPos.getZ())));
            // TODO: Save should happen only on server side 
            Rads.radiationAreaManager.save(Rads.radiationAreas, saveDirectory);
        }
    }

    @SubscribeEvent
    public void onEntityEnter(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (Rads.radiationAreas.size() > 0 && player.tickCount % 20 == 13) {
            for (RadiationArea area : Rads.radiationAreas) {
                if (area.isInside(player)) {
                    player.hurt(area.getDamageSource(player), area.getDamageAmount());
                    break;
                }
            }
        } 
    }

    // You can use EventBusSubscriber to automatically register all static methods
    // in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("This is Rads client setup maybe do something with this in the future?");
            LOGGER.info("Welcome to Rads, {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
