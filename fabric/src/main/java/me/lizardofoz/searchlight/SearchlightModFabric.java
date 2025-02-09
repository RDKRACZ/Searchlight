package me.lizardofoz.searchlight;

import com.google.common.collect.ImmutableMap;
import me.lizardofoz.searchlight.block.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public final class SearchlightModFabric extends SearchlightMod implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        creativeItemGroup = FabricItemGroupBuilder.build(
                new Identifier("searchlight", "searchlight"),
                () -> new ItemStack(searchlightBlock));

        blockEntitySynchronizer = blockEntity -> {
            BlockState state = blockEntity.getCachedState();
            World world = blockEntity.getWorld();
            if (world != null)
                world.updateListeners(blockEntity.getPos(), state, state, 2);
        };
        blockEntityConstructor = SearchlightBlockEntityFabric::new;

        registerSearchlightBlock();
        registerSearchlightLightSourceBlock();
        registerWallLightBlocks();
    }

    private void registerSearchlightBlock()
    {
        searchlightBlock = new SearchlightBlock(
                FabricBlockSettings.of(Material.METAL, MapColor.CLEAR)
                        .sounds(BlockSoundGroup.METAL)
                        .breakByTool(FabricToolTags.PICKAXES, 2)
                        .requiresTool()
                        .strength(4)
                        .nonOpaque());
        searchlightItem = new BlockItem(searchlightBlock, new FabricItemSettings().group(creativeItemGroup));
        searchlightBlockEntityType = FabricBlockEntityTypeBuilder.create(SearchlightBlockEntityFabric::create, searchlightBlock).build(null);

        Registry.register(Registry.BLOCK, new Identifier("searchlight", "searchlight"), searchlightBlock);
        Registry.register(Registry.ITEM, new Identifier("searchlight", "searchlight"), searchlightItem);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier("searchlight", "searchlight_entity"), searchlightBlockEntityType);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            BlockEntityRendererRegistry.INSTANCE.register(searchlightBlockEntityType, SearchlightBlockRenderer::new);
    }

    private void registerSearchlightLightSourceBlock()
    {
        lightSourceBlock = new SearchlightLightSourceBlock(
                AbstractBlock.Settings.of(
                        new FabricMaterialBuilder(MapColor.CLEAR)
                                .replaceable()
                                .lightPassesThrough()
                                .notSolid()
                                .build())
                        .sounds(BlockSoundGroup.WOOD)
                        .strength(3600000.8F)
                        .dropsNothing()
                        .nonOpaque()
                        .luminance((state) -> 15));
        lightSourceBlockEntityType = FabricBlockEntityTypeBuilder.create(SearchlightLightSourceBlockEntity::new, lightSourceBlock).build(null);

        Registry.register(Registry.BLOCK, new Identifier("searchlight", "searchlight_lightsource"), lightSourceBlock);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier("searchlight", "searchlight_lightsource_entity"), lightSourceBlockEntityType);
    }

    private void registerWallLightBlocks()
    {
        Map<Block, Item> wallLights = new HashMap<>();
        registerWallLight("iron", wallLights);
        registerWallLight("copper", wallLights);
        registerWallLight("prismarine", wallLights);
        for (DyeColor color : DyeColor.values())
            registerWallLight(color.getName(), wallLights);
        wallLightBlocks = ImmutableMap.copyOf(wallLights);
    }

    private void registerWallLight(String postfix, Map<Block,Item> wallLightMap)
    {
        Block block = new WallLightBlock(
                AbstractBlock.Settings.of(Material.DECORATION)
                        .strength(0.5F)
                        .luminance((state) -> 14)
                        .sounds(BlockSoundGroup.STONE)
                        .nonOpaque()
                        .noCollision());
        Item item = new BlockItem(block, new FabricItemSettings().group(creativeItemGroup));

        Registry.register(Registry.BLOCK, new Identifier("searchlight", "wall_light_" + postfix), block);
        Registry.register(Registry.ITEM, new Identifier("searchlight", "wall_light_" + postfix), item);
        wallLightMap.put(block, item);
    }
}
