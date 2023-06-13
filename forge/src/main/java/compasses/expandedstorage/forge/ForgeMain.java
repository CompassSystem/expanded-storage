package compasses.expandedstorage.forge;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import compasses.expandedstorage.common.CommonMain;
import compasses.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import compasses.expandedstorage.common.block.misc.BasicLockable;
import compasses.expandedstorage.common.block.misc.CopperBlockHelper;
import compasses.expandedstorage.common.block.strategies.ItemAccess;
import compasses.expandedstorage.common.misc.Utils;
import compasses.expandedstorage.common.recipe.ConversionRecipeManager;
import compasses.expandedstorage.common.recipe.ConversionRecipeReloadListener;
import compasses.expandedstorage.common.registration.Content;
import compasses.expandedstorage.common.registration.NamedValue;
import compasses.expandedstorage.forge.block.misc.ChestItemAccess;
import compasses.expandedstorage.forge.block.misc.GenericItemAccess;
import compasses.expandedstorage.forge.item.ChestBlockItem;
import compasses.expandedstorage.forge.item.ChestMinecartItem;
import compasses.expandedstorage.forge.item.MiniStorageBlockItem;
import compasses.expandedstorage.forge.misc.ForgePlatformHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Mod("expandedstorage")
public final class ForgeMain {
    public ForgeMain() {
        CommonMain.constructContent(new ForgePlatformHelper(), GenericItemAccess::new, BasicLockable::new,
                FMLLoader.getDist().isClient(), this::registerContent, "Forge", "Forge", // Don't think there's any project to load forge mods on other platforms yet.
                /*Base*/ false,
                /*Chest*/ ChestBlockItem::new, ChestItemAccess::new,
                /*Minecart Chest*/ ChestMinecartItem::new,
                /*Old Chest*/
                /*Barrel*/ TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("forge", "barrels/wooden")),
                /*Mini Storage*/ MiniStorageBlockItem::new);
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new ConversionRecipeReloadListener()));
        MinecraftForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> CommonMain.platformHelper().sendConversionRecipesToClient(event.getPlayer(), ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes()));

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, (AttachCapabilitiesEvent<BlockEntity> event) -> {
            if (event.getObject() instanceof OpenableBlockEntity entity) {
                event.addCapability(Utils.id("item_access"), new ICapabilityProvider() {
                    @NotNull
                    @Override
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                        if (capability == ForgeCapabilities.ITEM_HANDLER) {
                            return LazyOptional.of(() -> {
                                //noinspection unchecked
                                return (T) CommonMain.getItemAccess(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity).map(ItemAccess::get).orElseThrow();
                            });
                        }
                        return LazyOptional.empty();
                    }
                });
            }
        });

        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteractSpecific event) -> {
            InteractionResult result = CommonMain.interactWithEntity(event.getLevel(), event.getEntity(), event.getHand(), event.getTarget());
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        });

        FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterEvent event) -> {
            event.register(ForgeRegistries.Keys.MENU_TYPES, helper -> {
                helper.register(Utils.HANDLER_TYPE_ID, CommonMain.platformHelper().getScreenHandlerType());
            });
        });
    }

    private void registerContent(Content content) {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((RegisterEvent event) -> {
            event.register(ForgeRegistries.Keys.STAT_TYPES, helper -> {
                content.getStats().forEach(it -> Registry.register(BuiltInRegistries.CUSTOM_STAT, it, it));
            });

            event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
                CommonMain.iterateNamedList(content.getBlocks(), helper::register);
            });

            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                CommonMain.iterateNamedList(content.getItems(), helper::register);
            });

            event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
                ForgeMain.registerBlockEntity(helper, content.getChestBlockEntityType());
                ForgeMain.registerBlockEntity(helper, content.getOldChestBlockEntityType());
                ForgeMain.registerBlockEntity(helper, content.getBarrelBlockEntityType());
                ForgeMain.registerBlockEntity(helper, content.getMiniChestBlockEntityType());
            });

            event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
                CommonMain.iterateNamedList(content.getEntityTypes(), helper::register);
            });

            event.register(BuiltInRegistries.CREATIVE_MODE_TAB.key(), helper -> {
                helper.register(Utils.id("tab"), CreativeModeTab.builder()
                                                                .icon(() -> ForgeRegistries.ITEMS.getValue(Utils.id("netherite_chest")).getDefaultInstance())
                                                                .displayItems((displayParameters, output) -> {
                                                                    CommonMain.generateDisplayItems(displayParameters, output::accept);
                                                                })
                                                                .title(Component.translatable("itemGroup.expandedstorage.tab"))
                                                                .build());
            });
        });

        // Hopefully if another mod replaces this supplier we'll capture theirs here.
        Supplier<BiMap<Block, Block>> originalWaxablesMap = HoneycombItem.WAXABLES;
        HoneycombItem.WAXABLES = Suppliers.memoize(() -> {
            return ImmutableBiMap.<Block, Block>builder()
                                 // Hopefully the original / modded map is okay to query here.
                                 .putAll(originalWaxablesMap.get())
                                 .putAll(CopperBlockHelper.dewaxing().inverse())
                                 .build();
        });

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ForgeClient.initialize(modBus, content);
        }
    }

    private static <T extends BlockEntity> void registerBlockEntity(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper, NamedValue<BlockEntityType<T>> blockEntityType) {
        helper.register(blockEntityType.getName(), blockEntityType.getValue());
    }
}
