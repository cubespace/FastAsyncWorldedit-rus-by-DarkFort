package com.boydti.fawe.object.brush.heightmap;

import com.boydti.fawe.object.PseudoRandom;
import com.boydti.fawe.util.MainUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public interface HeightMap {
    public double getHeight(int x, int z);

    public void setSize(int size);


    default void perform(EditSession session, Mask mask, Vector pos, int size, int rotationMode, double yscale, boolean smooth, boolean towards, boolean layers) throws MaxChangedBlocksException {
        int[][] data = generateHeightData(session, mask, pos, size, rotationMode, yscale, smooth, towards, layers);
        applyHeightMapData(data, session, mask, pos, size, rotationMode, yscale, smooth, towards, layers);
    }

    default void applyHeightMapData(int[][] data, EditSession session, Mask mask, Vector pos, int size, int rotationMode, double yscale, boolean smooth, boolean towards, boolean layers) throws MaxChangedBlocksException {
        Vector top = session.getMaximumPoint();
        int maxY = top.getBlockY();
        int diameter = 2 * size + 1;
        int iterations = 1;
        WorldVector min = new WorldVector(LocalWorldAdapter.adapt(session.getWorld()), pos.subtract(size, maxY, size));
        Vector max = pos.add(size, maxY, size);
        Region region = new CuboidRegion(session.getWorld(), min, max);
        com.sk89q.worldedit.math.convolution.HeightMap heightMap = new com.sk89q.worldedit.math.convolution.HeightMap(session, region, data[0], layers);
        if (smooth) {
            try {
                HeightMapFilter filter = (HeightMapFilter) HeightMapFilter.class.getConstructors()[0].newInstance(GaussianKernel.class.getConstructors()[0].newInstance(5, 1));
                data[1] = filter.filter(data[1], diameter, diameter);
            } catch (Throwable e) {
                MainUtil.handleError(e);
            }
        }
        try {
            if (layers) {
                heightMap.applyLayers(data[1]);
            } else {
                heightMap.apply(data[1]);
            }
        } catch (MaxChangedBlocksException e) {
            throw e;
        } catch (WorldEditException e2) {
            throw new RuntimeException(e2);
        }
    }

    default int[][] generateHeightData(EditSession session, Mask mask, Vector pos, int size, int rotationMode, double yscale, boolean smooth, boolean towards, final boolean layers) {
        Vector top = session.getMaximumPoint();
        int maxY = top.getBlockY();
        int diameter = 2 * size + 1;
        int centerX = pos.getBlockX();
        int centerZ = pos.getBlockZ();
        int centerY = pos.getBlockY();
        int endY = pos.getBlockY() + size;
        int startY = pos.getBlockY() - size;
        int[] oldData = new int[diameter * diameter];
        int[] newData = new int[oldData.length];
        if (layers) { // Pixel accuracy
            centerY <<= 3;
            maxY <<= 3;
        }
        Vector mutablePos = new Vector(0, 0, 0);
        if (towards) {
            double sizePow = Math.pow(size, yscale);
            int targetY = pos.getBlockY();
            for (int x = -size; x <= size; x++) {
                int xx = centerX + x;
                mutablePos.mutX(xx);
                for (int z = -size; z <= size; z++) {
                    int index = (z + size) * diameter + (x + size);
                    int zz = centerZ + z;
                    double raise;
                    switch (rotationMode) {
                        default:
                            raise = getHeight(x, z);
                            break;
                        case 1:
                            raise = getHeight(z, x);
                            break;
                        case 2:
                            raise = getHeight(-x, -z);
                            break;
                        case 3:
                            raise = getHeight(-z, -x);
                            break;
                    }
                    int height;
                    if (layers) {
                        height = session.getNearestSurfaceLayer(xx, zz, pos.getBlockY(), 0, maxY);
                    } else {
                        height = session.getNearestSurfaceTerrainBlock(xx, zz, pos.getBlockY(), 0, maxY);
                    }
                    oldData[index] = height;
                    if (height == 0) {
                        newData[index] = centerY;
                        continue;
                    }
                    double raisePow = Math.pow(raise, yscale);
                    int diff = targetY - height;
                    double raiseScaled = diff * (raisePow / sizePow);
                    double raiseScaledAbs = Math.abs(raiseScaled);
                    int random = PseudoRandom.random.random(256) < (int) ((Math.ceil(raiseScaledAbs) - Math.floor(raiseScaledAbs)) * 256) ? (diff > 0 ? 1 : -1) : 0;
                    int raiseScaledInt = (int) raiseScaled + random;
                    newData[index] = height + raiseScaledInt;
                }
            }
        } else {
            int height = pos.getBlockY();
            for (int x = -size; x <= size; x++) {
                int xx = centerX + x;
                mutablePos.mutX(xx);
                for (int z = -size; z <= size; z++) {
                    int index = (z + size) * diameter + (x + size);
                    int zz = centerZ + z;
                    double raise;
                    switch (rotationMode) {
                        default:
                            raise = getHeight(x, z);
                            break;
                        case 1:
                            raise = getHeight(z, x);
                            break;
                        case 2:
                            raise = getHeight(-x, -z);
                            break;
                        case 3:
                            raise = getHeight(-z, -x);
                            break;
                    }
                    if (layers) {
                        height = session.getNearestSurfaceLayer(xx, zz, height, 0, maxY);
                    } else {
                        height = session.getNearestSurfaceTerrainBlock(xx, zz, height, 0, 255);
                    }
                    oldData[index] = height;
                    if (height == 0) {
                        newData[index] = centerY;
                        continue;
                    }
                    raise = (yscale * raise);
                    int random = PseudoRandom.random.random(256) < (int) ((raise - (int) raise) * (256)) ? 1 : 0;
                    int newHeight = height + (int) raise + random;
                    newData[index] = newHeight;
                }
            }
        }
        return new int[][] {oldData, newData};
    }
}
