package ellemes.expandedstorage.forge;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import ellemes.expandedstorage.common.CommonMain;
import ellemes.expandedstorage.common.block.misc.CopperBlockHelper;
import ellemes.expandedstorage.common.block.strategies.ItemAccess;
import ellemes.expandedstorage.common.misc.TagReloadListener;
import ellemes.expandedstorage.common.misc.TieredObject;
import ellemes.expandedstorage.common.misc.Utils;
import ellemes.expandedstorage.common.block.entity.extendable.OpenableBlockEntity;
import ellemes.expandedstorage.common.block.misc.BasicLockable;
import ellemes.expandedstorage.forge.block.misc.ChestItemAccess;
import ellemes.expandedstorage.forge.block.misc.GenericItemAccess;
import ellemes.expandedstorage.forge.item.ChestBlockItem;
import ellemes.expandedstorage.forge.item.ChestMinecartItem;
import ellemes.expandedstorage.forge.item.MiniChestBlockItem;
import ellemes.expandedstorage.common.registration.Content;
import ellemes.expandedstorage.common.registration.NamedValue;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Mod("expandedstorage")
public final class ForgeMain {

    public ForgeMain() {
        new ellemes.container_library.forge.ForgeMain();

        TagReloadListener tagReloadListener = new TagReloadListener();

        CommonMain.constructContent(GenericItemAccess::new, BasicLockable::new,
                new CreativeModeTab(Utils.MOD_ID + ".tab") {
                    @NotNull
                    @Override
                    public ItemStack makeIcon() {
                        return new ItemStack(ForgeRegistries.ITEMS.getValue(Utils.id("netherite_chest")), 1);
                    }
                }, FMLLoader.getDist().isClient(), tagReloadListener, this::registerContent,
                /*Base*/ false,
                /*Chest*/ TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("forge", "chests/wooden")), ChestBlockItem::new, ChestItemAccess::new,
                /*Minecart Chest*/ ChestMinecartItem::new,
                /*Old Chest*/
                /*Barrel*/ TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("forge", "barrels/wooden")),
                /*Mini Chest*/ MiniChestBlockItem::new);

        MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> tagReloadListener.postDataReload());

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
            InteractionResult result = CommonMain.interactWithEntity(event.getLevel(), event.getEntity(), event.getHand(), event.getTarget());
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        });
    }

    private void registerContent(Content content) {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((RegisterEvent event) -> {
            event.register(ForgeRegistries.Keys.STAT_TYPES, helper -> {
                content.getStats().forEach(it -> Registry.register(Registry.CUSTOM_STAT, it, it));
            });

            event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
                CommonMain.iterateNamedList(content.getBlocks(), (name, value) -> {
                    helper.register(name, value);
                    CommonMain.registerTieredObject(value);
                });
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
                CommonMain.iterateNamedList(content.getEntityTypes(), (name, value) -> {
                    helper.register(name, value);
                    if (value instanceof TieredObject object) {
                        CommonMain.registerTieredObject(object);
                    }
                });
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
            ForgeClient.initialize();
            ForgeClient.registerListeners(modBus, content);
        }
    }

    private static <T extends BlockEntity> void registerBlockEntity(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper, NamedValue<BlockEntityType<T>> blockEntityType) {
        helper.register(blockEntityType.getName(), blockEntityType.getValue());
    }
}
