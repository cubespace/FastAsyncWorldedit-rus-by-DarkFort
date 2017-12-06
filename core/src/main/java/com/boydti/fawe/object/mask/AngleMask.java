package com.boydti.fawe.object.mask;

import com.sk89q.worldedit.MutableBlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import java.util.Arrays;
import javax.annotation.Nullable;

public class AngleMask extends SolidBlockMask implements ResettableMask {
    public static double ADJACENT_MOD = 0.5;
    public static double DIAGONAL_MOD = 1 / Math.sqrt(8);

    private final CachedMask mask;
    private final double max;
    private final double min;
    private final boolean overlay;
    private int maxY;

    private transient MutableBlockVector mutable = new MutableBlockVector();

    public AngleMask(Extent extent, double min, double max, boolean overlay) {
        super(extent);
        this.mask = new CachedMask(new SolidBlockMask(extent));
        this.min = min;
        this.max = max;
        this.maxY = extent.getMaximumPoint().getBlockY();
        this.overlay = overlay;
    }

    @Override
    public void reset() {
        mutable = new MutableBlockVector();
        cacheBotX = Integer.MIN_VALUE;
        cacheBotZ = Integer.MIN_VALUE;
        lastX = Integer.MIN_VALUE;
        lastX = Integer.MIN_VALUE;
        if (cacheHeights != null) {
            Arrays.fill(cacheHeights, (byte) 0);
        }
    }

    private transient int cacheCenX;
    private transient int cacheCenZ;
    private transient int cacheBotX = Integer.MIN_VALUE;
    private transient int cacheBotZ = Integer.MIN_VALUE;
    private transient int cacheCenterZ;

    private transient byte[] cacheHeights;
    private transient byte[] cacheDistance;

    private transient int lastY;
    private transient int lastX = Integer.MIN_VALUE;
    private transient int lastZ = Integer.MIN_VALUE;
    private transient boolean foundY;
    private transient boolean lastValue;

    public int getHeight(int x, int y, int z) {
//        return getExtent().getNearestSurfaceTerrainBlock(x, z, y, 0, maxY);
        try {
            int rx = x - cacheBotX + 16;
            int rz = z - cacheBotZ + 16;
            int index;
            if (((rx & 0xFF) != rx || (rz & 0xFF) != rz)) {
                cacheBotX = x - 16;
                cacheBotZ = z - 16;
                rx = x - cacheBotX + 16;
                rz = z - cacheBotZ + 16;
                index = rx + (rz << 8);
                if (cacheHeights == null) {
                    cacheHeights = new byte[65536];
                    cacheDistance = new byte[65536];
                } else {
                    Arrays.fill(cacheHeights, (byte) 0);
                    Arrays.fill(cacheDistance, (byte) 0);
                }
            } else {
                index = rx + (rz << 8);
            }
            int result = cacheHeights[index] & 0xFF;
            int distance = cacheDistance[index] & 0xFF;
            if (result == 0 || distance < Math.abs(result - y)) {
                cacheHeights[index] = (byte) (result = lastY = getExtent().getNearestSurfaceTerrainBlock(x, z, lastY, 0, maxY));
                cacheDistance[index] = (byte) Math.abs(result - y);
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean testSlope(int x, int y, int z) {
        double slope;
        boolean aboveMin;
        if ((lastX == (lastX = x) & lastZ == (lastZ = z))) {
            return lastValue;
        }
        slope = Math.abs(getHeight(x + 1, y, z) - getHeight(x - 1, y, z)) * ADJACENT_MOD;
        if (slope >= min && max >= Math.max(maxY - y, y)) {
            return lastValue = true;
        }
        slope = Math.max(slope, Math.abs(getHeight(x, y, z + 1) - getHeight(x, y, z - 1)) * ADJACENT_MOD);
        slope = Math.max(slope, Math.abs(getHeight(x + 1, y, z + 1) - getHeight(x - 1, y, z - 1)) * DIAGONAL_MOD);
        slope = Math.max(slope, Math.abs(getHeight(x - 1, y, z + 1) - getHeight(x + 1, y, z - 1)) * DIAGONAL_MOD);
        return lastValue = (slope >= min && slope <= max);
    }

    public boolean adjacentAir(Vector v) {
        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();
        if (!mask.test(x + 1, y, z)) {
            return true;
        }
        if (!mask.test(x - 1, y, z)) {
            return true;
        }
        if (!mask.test(x, y, z + 1)) {
            return true;
        }
        if (!mask.test(x, y, z - 1)) {
            return true;
        }
        if (y < 255 && !mask.test(x, y + 1, z)) {
            return true;
        }
        if (y > 0 && !mask.test(x, y - 1, z)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean test(Vector vector) {
        int x = vector.getBlockX();
        int y = vector.getBlockY();
        int z = vector.getBlockZ();
        if (!mask.test(x, y, z)) {
            return false;
        }
        if (overlay) {
            if (y < 255 && !mask.test(x, y + 1, z)) return lastValue = false;
        } else if (!adjacentAir(vector)) {
            return false;
        }
        return testSlope(x, y, z);
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        return null;
    }
}
