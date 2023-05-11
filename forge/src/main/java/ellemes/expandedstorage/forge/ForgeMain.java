package ellemes.expandedstorage.forge;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.common.block.misc.BasicLockable;
import ellemes.expandedstorage.common.block.misc.CopperBlockHelper;
import ellemes.expandedstorage.common.block.strategies.ItemAccess;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.recipe.ConversionRecipeManager;
import ellemes.expandedstorage.common.recipe.ConversionRecipeReloadListener;
import ellemes.expandedstorage.common.registration.Content;
import ellemes.expandedstorage.common.registration.NamedValue;
import ellemes.expandedstorage.forge.block.misc.ChestItemAccess;
import ellemes.expandedstorage.forge.block.misc.GenericItemAccess;
import ellemes.expandedstorage.forge.item.ChestBlockItem;
import ellemes.expandedstorage.forge.item.ChestMinecartItem;
import ellemes.expandedstorage.forge.item.MiniStorageBlockItem;
import ellemes.expandedstorage.forge.misc.ForgePlatformHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Mod("expandedstorage")
public final class ForgeMain {
    public ForgeMain() {
        CommonMain.constructContent(new ForgePlatformHelper(), GenericItemAccess::new, BasicLockable::new,
                new CreativeModeTab(Utils.MOD_ID + ".tab") {
                    @NotNull
                    @Override
                    public ItemStack makeIcon() {
                        return new ItemStack(ForgeRegistries.ITEMS.getValue(Utils.id("netherite_chest")), 1);
                    }
                }, FMLLoader.getDist().isClient(), this::registerContent,
                /*Base*/ false,
                /*Chest*/ ChestBlockItem::new, ChestItemAccess::new,
                /*Minecart Chest*/ ChestMinecartItem::new,
                /*Old Chest*/
                /*Barrel*/ TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("forge", "barrels/wooden")),
                /*Mini Storage*/ MiniStorageBlockItem::new);
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new ConversionRecipeReloadListener()));
        MinecraftForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> CommonMain.platformHelper().sendConversionRecipesToClient(event.getPlayer(), ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes()));

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, (AttachCapabilitiesEvent<BlockEntity> event) -> {
            if (event.getObject() instanceof OpenableBlockEntity entity) {
                event.addCapability(Utils.id("item_access"), new ICapabilityProvider() {
                    @NotNull
                    @Override
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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
            InteractionResult result = CommonMain.interactWithEntity(event.getWorld(), event.getPlayer(), event.getHand(), event.getTarget());
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        });

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(MenuType.class, (RegistryEvent.Register<MenuType<?>> event) -> {
            IForgeRegistry<MenuType<?>> registry = event.getRegistry();

            registry.registerAll(CommonMain.platformHelper().getScreenHandlerType());
        });
    }

    private void registerContent(Content content) {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addGenericListener(StatType.class, (RegistryEvent.Register<StatType<?>> event) -> {
            content.getStats().forEach(it -> Registry.register(Registry.CUSTOM_STAT, it, it));
        });

        modBus.addGenericListener(Block.class, (RegistryEvent.Register<Block> event) -> {
            IForgeRegistry<Block> registry = event.getRegistry();
            CommonMain.iterateNamedList(content.getBlocks(), (name, value) -> {
                registry.register(value.setRegistryName(name));
            });
        });

        modBus.addGenericListener(Item.class, (RegistryEvent.Register<Item> event) -> {
            IForgeRegistry<Item> registry = event.getRegistry();
            CommonMain.iterateNamedList(content.getItems(), (name, value) -> {
                registry.register(value.setRegistryName(name));
            });
        });

        modBus.addGenericListener(BlockEntityType.class, (RegistryEvent.Register<BlockEntityType<?>> event) -> {
            IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
            ForgeMain.registerBlockEntity(registry, content.getChestBlockEntityType());
            ForgeMain.registerBlockEntity(registry, content.getOldChestBlockEntityType());
            ForgeMain.registerBlockEntity(registry, content.getBarrelBlockEntityType());
            ForgeMain.registerBlockEntity(registry, content.getMiniChestBlockEntityType());
        });

        modBus.addGenericListener(EntityType.class, (RegistryEvent.Register<EntityType<?>> event) -> {
            IForgeRegistry<EntityType<?>> registry = event.getRegistry();
            CommonMain.iterateNamedList(content.getEntityTypes(), (name, value) -> {
                registry.register(value.setRegistryName(name));
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

    private static <T extends BlockEntity> void registerBlockEntity(IForgeRegistry<BlockEntityType<?>> registry, NamedValue<BlockEntityType<T>> blockEntityType) {
        registry.register(blockEntityType.getValue().setRegistryName(blockEntityType.getName()));
    }
}
