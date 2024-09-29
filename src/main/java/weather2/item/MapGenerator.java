package weather2.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MapGenerator {
    private static final Executor ASYNC_EXECUTOR = Executors.newCachedThreadPool();

    public void prefillMapAsync(MapItemSavedData mapData, ServerLevel serverLevel) {
        // Offload the map generation to a separate thread
        CompletableFuture.runAsync(() -> prefillMap(mapData, serverLevel), ASYNC_EXECUTOR)
                .thenRun(() -> {
                    // Mark the map data as modified on the main thread
                    serverLevel.getServer().execute(mapData::setDirty);
                });
    }

    private void prefillMap(MapItemSavedData mapData, ServerLevel serverLevel) {
        // Set the map to be fully explored
        int mapSize = 128;
        int scale = 1 << mapData.scale;
        int centerX = mapData.centerX;
        int centerZ = mapData.centerZ;

        boolean hasCeiling = serverLevel.dimensionType().hasCeiling();

        // Loop through each pixel of the map
        for (int x = 0; x < mapSize; x++) {
            for (int z = 0; z < mapSize; z++) {
                int worldX = (centerX / scale - mapSize / 2) + x;
                int worldZ = (centerZ / scale - mapSize / 2) + z;

                BlockPos pos = new BlockPos(worldX * scale, 0, worldZ * scale);

                // Get the height for the surface
                BlockPos surfacePos = serverLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);
                BlockState blockState = serverLevel.getBlockState(surfacePos);
                MapColor mapColor = blockState.getMapColor(serverLevel, surfacePos);

                // Handle ceiling dimension
                double avgHeight = 0.0;
                int fluidBlockCount = 0;
                Multiset<MapColor> colorMultiset = LinkedHashMultiset.create();
                if (hasCeiling) {
                    colorMultiset.add(Blocks.DIRT.defaultBlockState().getMapColor(serverLevel, BlockPos.ZERO), 10);
                    avgHeight = 100.0;
                } else {
                    for (int i = 0; i < scale; i++) {
                        for (int j = 0; j < scale; j++) {
                            BlockPos checkPos = surfacePos.offset(i, 0, j);
                            int blockHeight = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, checkPos.getX(), checkPos.getZ());
                            BlockState stateAtPos = serverLevel.getBlockState(new BlockPos(checkPos.getX(), blockHeight - 1, checkPos.getZ()));

                            if (blockHeight <= serverLevel.getMinBuildHeight() + 1) {
                                stateAtPos = Blocks.BEDROCK.defaultBlockState();
                            } else {
                                // Handle fluid blocks properly by looking below the surface
                                BlockState belowState;
                                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(checkPos.getX(), blockHeight - 1, checkPos.getZ());
                                do {
                                    belowState = serverLevel.getBlockState(mutablePos.move(Direction.DOWN));
                                    fluidBlockCount++;
                                } while (!belowState.getFluidState().isEmpty() && mutablePos.getY() > serverLevel.getMinBuildHeight());

                                // Handle fluid rendering appropriately
                                stateAtPos = serverLevel.getFluidState(checkPos).isEmpty() ? stateAtPos : belowState;
                            }

                            avgHeight += blockHeight / (double)(scale * scale);
                            colorMultiset.add(stateAtPos.getMapColor(serverLevel, checkPos));
                        }
                    }
                }

                MapColor dominantColor = Iterables.getFirst(Multisets.copyHighestCountFirst(colorMultiset), MapColor.NONE);
                MapColor.Brightness brightness = MapColor.Brightness.NORMAL;

                // Adjust brightness for water and height differences
                if (dominantColor == MapColor.WATER) {
                    double adjustedHeight = (double) fluidBlockCount * 0.1D + (double)((x + z) & 1) * 0.2D;
                    if (adjustedHeight < 0.5D) {
                        brightness = MapColor.Brightness.HIGH;
                    } else if (adjustedHeight > 0.9D) {
                        brightness = MapColor.Brightness.LOW;
                    }
                } else {
                    double heightDifference = (avgHeight - 0) * 4.0D / (double)(scale + 4) + ((x + z) & 1) * 0.4D;
                    if (heightDifference > 0.6D) {
                        brightness = MapColor.Brightness.HIGH;
                    } else if (heightDifference < -0.6D) {
                        brightness = MapColor.Brightness.LOW;
                    }
                }

                // Update the map pixel with the correct color and brightness
                mapData.colors[x + z * mapSize] = (byte) dominantColor.getPackedId(brightness);
            }
        }
    }
}
