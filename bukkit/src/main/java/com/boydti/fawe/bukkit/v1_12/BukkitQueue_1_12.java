package com.boydti.fawe.bukkit.v1_12;

import com.boydti.fawe.Fawe;
import com.boydti.fawe.FaweCache;
import com.boydti.fawe.bukkit.BukkitPlayer;
import com.boydti.fawe.bukkit.v0.BukkitQueue_0;
import com.boydti.fawe.example.CharFaweChunk;
import com.boydti.fawe.object.FaweChunk;
import com.boydti.fawe.object.FawePlayer;
import com.boydti.fawe.object.RegionWrapper;
import com.boydti.fawe.object.RunnableVal;
import com.boydti.fawe.object.brush.visualization.VisualChunk;
import com.boydti.fawe.object.collection.PrimitiveList;
import com.boydti.fawe.object.visitor.FaweChunkVisitor;
import com.boydti.fawe.util.MainUtil;
import com.boydti.fawe.util.MathMan;
import com.boydti.fawe.util.ReflectionUtils;
import com.boydti.fawe.util.TaskManager;
import com.boydti.fawe.util.task.TaskBuilder;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.biome.BaseBiome;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BiomeCache;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_12_R1.ChunkProviderGenerate;
import net.minecraft.server.v1_12_R1.ChunkProviderServer;
import net.minecraft.server.v1_12_R1.ChunkRegionLoader;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.DataPaletteBlock;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EntityTracker;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.EnumDifficulty;
import net.minecraft.server.v1_12_R1.EnumGamemode;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.IChunkLoader;
import net.minecraft.server.v1_12_R1.IDataManager;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NibbleArray;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_12_R1.PlayerChunk;
import net.minecraft.server.v1_12_R1.PlayerChunkMap;
import net.minecraft.server.v1_12_R1.RegionFile;
import net.minecraft.server.v1_12_R1.RegionFileCache;
import net.minecraft.server.v1_12_R1.ServerNBTManager;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.WorldChunkManager;
import net.minecraft.server.v1_12_R1.WorldData;
import net.minecraft.server.v1_12_R1.WorldManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import net.minecraft.server.v1_12_R1.WorldSettings;
import net.minecraft.server.v1_12_R1.WorldType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

public class BukkitQueue_1_12 extends BukkitQueue_0<net.minecraft.server.v1_12_R1.Chunk, ChunkSection[], ChunkSection> {

    protected static IBlockData air;
    protected static Field fieldBits;
    protected static Field fieldPalette;
    protected static Field fieldSize;
    protected static Method getEntitySlices;
    protected static Field fieldTickingBlockCount;
    protected static Field fieldNonEmptyBlockCount;
    protected static Field fieldSection;
    protected static Field fieldBiomes;
    protected static Field fieldChunkGenerator;
    protected static Field fieldSeed;
    protected static Field fieldBiomeCache;
    protected static Field fieldBiomes2;
    protected static Field fieldGenLayer1;
    protected static Field fieldGenLayer2;
    protected static Field fieldChunks;
    protected static Field fieldChunkLoader;
    protected static MutableGenLayer genLayer;
    protected static ChunkSection emptySection;

    public static final IBlockData[] IBD_CACHE = new IBlockData[Character.MAX_VALUE + 1];

    static {
        try {
            emptySection = new ChunkSection(0, true);
            fieldSection = ChunkSection.class.getDeclaredField("blockIds");
            fieldTickingBlockCount = ChunkSection.class.getDeclaredField("tickingBlockCount");
            fieldNonEmptyBlockCount = ChunkSection.class.getDeclaredField("nonEmptyBlockCount");
            fieldSection.setAccessible(true);
            fieldTickingBlockCount.setAccessible(true);
            fieldNonEmptyBlockCount.setAccessible(true);

            fieldBiomes = ChunkProviderGenerate.class.getDeclaredField("D");
            fieldBiomes.setAccessible(true);
            fieldChunkGenerator = ChunkProviderServer.class.getDeclaredField("chunkGenerator");
            fieldChunkGenerator.setAccessible(true);
            fieldSeed = WorldData.class.getDeclaredField("e");
            fieldSeed.setAccessible(true);
            fieldBiomeCache = WorldChunkManager.class.getDeclaredField("d");
            fieldBiomeCache.setAccessible(true);
            fieldBiomes2 = WorldChunkManager.class.getDeclaredField("e");
            fieldBiomes2.setAccessible(true);
            fieldGenLayer1 = WorldChunkManager.class.getDeclaredField("b") ;
            fieldGenLayer2 = WorldChunkManager.class.getDeclaredField("c") ;
            fieldGenLayer1.setAccessible(true);
            fieldGenLayer2.setAccessible(true);

            fieldChunks = ChunkProviderServer.class.getDeclaredField("chunks");
            fieldChunkLoader = ChunkProviderServer.class.getDeclaredField("chunkLoader");
            fieldChunks.setAccessible(true);
            fieldChunkLoader.setAccessible(true);

            fieldPalette = DataPaletteBlock.class.getDeclaredField("c");
            fieldPalette.setAccessible(true);
            fieldSize = DataPaletteBlock.class.getDeclaredField("e");
            fieldSize.setAccessible(true);

            Field fieldAir = DataPaletteBlock.class.getDeclaredField("a");
            fieldAir.setAccessible(true);
            air = (IBlockData) fieldAir.get(null);
            fieldBits = DataPaletteBlock.class.getDeclaredField("b");
            fieldBits.setAccessible(true);
            getEntitySlices = net.minecraft.server.v1_12_R1.Chunk.class.getDeclaredMethod("getEntitySlices");
            getEntitySlices.setAccessible(true);
            setupAdapter(new FaweAdapter_1_12());
            Fawe.debug("Using adapter: " + adapter);
            Fawe.debug("=========================================");
            for (int i = 0; i < IBD_CACHE.length; i++) {
                try {
                    IBD_CACHE[i] = Block.getById(i >> 4).fromLegacyData(i & 0xF);
                } catch (Throwable ignore) {}
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public BukkitQueue_1_12(final com.sk89q.worldedit.world.World world) {
        super(world);
        getImpWorld();
    }

    public BukkitQueue_1_12(final String world) {
        super(world);
        getImpWorld();
    }

    @Override
    public ChunkSection[] getSections(net.minecraft.server.v1_12_R1.Chunk chunk) {
        return chunk.getSections();
    }

    @Override
    public net.minecraft.server.v1_12_R1.Chunk loadChunk(World world, int x, int z, boolean generate) {
        ChunkProviderServer provider = ((CraftWorld) world).getHandle().getChunkProviderServer();
        if (generate) {
            return provider.getOrLoadChunkAt(x, z);
        } else {
            return provider.loadChunk(x, z);
        }
    }

    @Override
    public ChunkSection[] getCachedSections(World world, int cx, int cz) {
        net.minecraft.server.v1_12_R1.Chunk chunk = ((CraftWorld) world).getHandle().getChunkProviderServer().getChunkIfLoaded(cx, cz);
        if (chunk != null) {
            return chunk.getSections();
        }
        return null;
    }

    @Override
    public net.minecraft.server.v1_12_R1.Chunk getCachedChunk(World world, int cx, int cz) {
        return ((CraftWorld) world).getHandle().getChunkProviderServer().getChunkIfLoaded(cx, cz);
    }

    @Override
    public ChunkSection getCachedSection(ChunkSection[] chunkSections, int cy) {
        return chunkSections[cy];
    }

    @Override
    public void saveChunk(net.minecraft.server.v1_12_R1.Chunk chunk) {
        chunk.f(true); // Set Modified
        chunk.mustSave = true;
    }

    @Override
    public boolean regenerateChunk(World world, int x, int z, BaseBiome biome, Long seed) {
        if (biome != null) {
            try {
                if (seed == null) {
                    seed = world.getSeed();
                }
                nmsWorld.worldData.getSeed();
                boolean result;
                ChunkProviderGenerate generator = new ChunkProviderGenerate(nmsWorld, seed, false, "");
                Biome bukkitBiome = adapter.getBiome(biome.getId());
                BiomeBase base = BiomeBase.getBiome(biome.getId());
                fieldBiomes.set(generator, new BiomeBase[]{base});
                boolean cold = base.getTemperature() <= 1;
                net.minecraft.server.v1_12_R1.ChunkGenerator existingGenerator = nmsWorld.getChunkProviderServer().chunkGenerator;
                long existingSeed = world.getSeed();
                {
                    if (genLayer == null) genLayer = new MutableGenLayer(seed);
                    genLayer.set(biome.getId());
                    Object existingGenLayer1 = fieldGenLayer1.get(nmsWorld.getWorldChunkManager());
                    Object existingGenLayer2 = fieldGenLayer2.get(nmsWorld.getWorldChunkManager());
                    fieldGenLayer1.set(nmsWorld.getWorldChunkManager(), genLayer);
                    fieldGenLayer2.set(nmsWorld.getWorldChunkManager(), genLayer);

                    fieldSeed.set(nmsWorld.worldData, seed);

                    ReflectionUtils.setFailsafeFieldValue(fieldBiomeCache, this.nmsWorld.getWorldChunkManager(), new BiomeCache(this.nmsWorld.getWorldChunkManager()));

                    ReflectionUtils.setFailsafeFieldValue(fieldChunkGenerator, this.nmsWorld.getChunkProviderServer(), generator);

                    result = getWorld().regenerateChunk(x, z);

                    ReflectionUtils.setFailsafeFieldValue(fieldChunkGenerator, this.nmsWorld.getChunkProviderServer(), existingGenerator);

                    fieldSeed.set(nmsWorld.worldData, existingSeed);

                    fieldGenLayer1.set(nmsWorld.getWorldChunkManager(), existingGenLayer1);
                    fieldGenLayer2.set(nmsWorld.getWorldChunkManager(), existingGenLayer2);
                }
                return result;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return super.regenerateChunk(world, x, z, biome, seed);
    }

    private void unload(RegionWrapper allowed, PrimitiveList<Long> chunks) {
        ArrayDeque<net.minecraft.server.v1_12_R1.Chunk> queue = TaskManager.IMP.sync(new RunnableVal<ArrayDeque<net.minecraft.server.v1_12_R1.Chunk>>() {
            @Override
            public void run(ArrayDeque<net.minecraft.server.v1_12_R1.Chunk> value) {
                this.value = new ArrayDeque<>();
                ChunkProviderServer provider = nmsWorld.getChunkProviderServer();
                int bcx = (allowed.minX >> 9) << 5;
                int bcz = (allowed.minZ >> 9) << 5;
                int tcx = 31 + (allowed.maxX >> 9) << 5;
                int tcz = 31 + (allowed.maxZ >> 9) << 5;
                Iterator<net.minecraft.server.v1_12_R1.Chunk> iter = provider.a().iterator();
                while (iter.hasNext()) {
                    net.minecraft.server.v1_12_R1.Chunk chunk = iter.next();
                    int cx = chunk.locX;
                    int cz = chunk.locZ;
                    if (cx >= bcx && cx <= tcx && cz >= bcz && cz <= tcz) {
                        this.value.add(chunk);
                        if (getPlayerChunk(nmsWorld, cx, cz) != null) {
                            chunks.add(ChunkCoordIntPair.a(chunk.locX, chunk.locZ));
                        }
                    }
                }
            }
        });
        if (Fawe.get().getMainThread() == Thread.currentThread()) {
            ChunkProviderServer provider = nmsWorld.getChunkProviderServer();
            for (net.minecraft.server.v1_12_R1.Chunk chunk : queue) {
                provider.unloadChunk(chunk, true);
            }
            World world = getWorld();
            boolean autoSave = world.isAutoSave();
            world.setAutoSave(true);
            for (int i = 0; i < 50 && !provider.getName().endsWith(" 0"); i++) {
                provider.unloadChunks();
            }
            world.setAutoSave(autoSave);
        } else {
            new TaskBuilder().syncWhenFree(new TaskBuilder.SplitTask(50) {
                @Override
                public Object exec(Object previous) {
                    ChunkProviderServer provider = nmsWorld.getChunkProviderServer();
                    for (net.minecraft.server.v1_12_R1.Chunk chunk : queue) {
                        provider.unloadChunk(chunk, true);
                        split();
                    }
                    try {
                        IChunkLoader loader = (IChunkLoader) fieldChunkLoader.get(provider);
                        if (loader instanceof ChunkRegionLoader) {
                            ChunkRegionLoader crl = (ChunkRegionLoader) loader;
                            for (int i = 0; i < queue.size() && crl.a(); i++) {
                                split();
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }).build();
        }
    }

    @Override
    public boolean setMCA(Runnable whileLocked, final RegionWrapper allowed, boolean unload) {
        try {
            Object lock = new Object();
            ForkJoinPool pool = new ForkJoinPool();
            PrimitiveList<Long> chunks = new PrimitiveList<Long>(Long.class);
            if (unload && Fawe.get().getMainThread() != Thread.currentThread()) unload(allowed, chunks);
            Thread operation = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                        synchronized (RegionFileCache.class) {
                            whileLocked.run();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            TaskManager.IMP.sync(new RunnableVal<Object>() {
                @Override
                public void run(Object value) {
                    try {
                        synchronized (RegionFileCache.class) {
                            operation.start(); // Async
                            ChunkProviderServer provider = nmsWorld.getChunkProviderServer();
                            if (unload) unload(allowed, chunks);
                            provider.c();
                            if (unload) { // Unload regions
                                Map<File, RegionFile> map = RegionFileCache.a;
                                Iterator<Map.Entry<File, RegionFile>> iter = map.entrySet().iterator();
                                while (iter.hasNext()) {
                                    Map.Entry<File, RegionFile> entry = iter.next();
                                    RegionFile regionFile = entry.getValue();
                                    pool.submit(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                regionFile.c();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    iter.remove();
                                }
                                pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                            }
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            operation.join();

            new TaskBuilder().syncWhenFree(new TaskBuilder.SplitTask(5) {
                @Override
                public Object exec(Object previous) {
                    ChunkProviderServer provider = nmsWorld.getChunkProviderServer();
                    for (long pos : chunks) {
                        int x = (int) pos;
                        int z = (int) (pos >> 32);
                        net.minecraft.server.v1_12_R1.Chunk chunk = provider.getOrLoadChunkAt(x, z);
                        if (chunk != null) {
                            PlayerChunk pc = getPlayerChunk(nmsWorld, x, z);
                            sendChunk(pc, chunk, 0);
                        }
                        split();
                    }
                    return null;
                }
            }).buildAsync();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setHeightMap(FaweChunk chunk, byte[] heightMap) {
        CraftChunk craftChunk = (CraftChunk) chunk.getChunk();
        if (craftChunk != null) {
            int[] otherMap = craftChunk.getHandle().heightMap;
            for (int i = 0; i < heightMap.length; i++) {
                int newHeight = heightMap[i] & 0xFF;
                int currentHeight = otherMap[i];
                if (newHeight > currentHeight) {
                    otherMap[i] = newHeight;
                }
            }
        }
    }

    @Override
    public boolean next(int amount, long time) {
        return super.next(amount, time);
    }

    @Override
    public void setSkyLight(ChunkSection section, int x, int y, int z, int value) {
        section.getSkyLightArray().a(x & 15, y & 15, z & 15, value);
    }

    @Override
    public void setBlockLight(ChunkSection section, int x, int y, int z, int value) {
        section.getEmittedLightArray().a(x & 15, y & 15, z & 15, value);
    }

    @Override
    public World createWorld(final WorldCreator creator) {
        final String name = creator.name();
        ChunkGenerator generator = creator.generator();
        final CraftServer server = (CraftServer) Bukkit.getServer();
        final MinecraftServer console = server.getServer();
        final File folder = new File(server.getWorldContainer(), name);
        final World world = server.getWorld(name);
        final WorldType type = WorldType.getType(creator.type().getName());
        final boolean generateStructures = creator.generateStructures();
        if (world != null) {
            return world;
        }
        if (folder.exists() && !folder.isDirectory()) {
            throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
        }
        TaskManager.IMP.sync(new RunnableVal<Object>() {
            @Override
            public void run(Object value) {
                try {
                    Field field = CraftServer.class.getDeclaredField("worlds");
                    field.setAccessible(true);
                    Map<Object, Object> existing = (Map<Object, Object>) field.get(server);
                    if (!existing.getClass().getName().contains("SynchronizedMap")) {
                        field.set(server, Collections.synchronizedMap(existing));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        if (generator == null) {
            generator = server.getGenerator(name);
        }
        int dimension = 10 + console.worlds.size();
        boolean used = false;
        do {
            for (final WorldServer ws : console.worlds) {
                used = (ws.dimension == dimension);
                if (used) {
                    ++dimension;
                    break;
                }
            }
        } while (used);
        final boolean hardcore = false;
        final IDataManager sdm = new ServerNBTManager(server.getWorldContainer(), name, true, server.getHandle().getServer().dataConverterManager);
        WorldData worlddata = sdm.getWorldData();
        final WorldSettings worldSettings;
        if (worlddata == null) {
            worldSettings = new WorldSettings(creator.seed(), EnumGamemode.getById(server.getDefaultGameMode().getValue()), generateStructures, hardcore, type);
            worldSettings.setGeneratorSettings(creator.generatorSettings());
            worlddata = new WorldData(worldSettings, name);
        } else {
            worldSettings = null;
        }
        worlddata.checkName(name);
        final WorldServer internal = (WorldServer)new WorldServer(console, sdm, worlddata, dimension, console.methodProfiler, creator.environment(), generator).b();
        startSet(true); // Temporarily allow async chunk load since the world isn't added yet
        if (worldSettings != null) {
            internal.a(worldSettings);
        }
        endSet(true);
        internal.scoreboard = server.getScoreboardManager().getMainScoreboard().getHandle();
        internal.tracker = new EntityTracker(internal);
        internal.addIWorldAccess(new WorldManager(console, internal));
        internal.worldData.setDifficulty(EnumDifficulty.EASY);
        internal.setSpawnFlags(true, true);
        if (generator != null) {
            internal.getWorld().getPopulators().addAll(generator.getDefaultPopulators(internal.getWorld()));
        }
        // Add the world
        return TaskManager.IMP.sync(new RunnableVal<World>() {
            @Override
            public void run(World value) {
                console.worlds.add(internal);
                server.getPluginManager().callEvent(new WorldInitEvent(internal.getWorld()));
                server.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
                this.value = internal.getWorld();
            }
        });
    }

    @Override
    public int getCombinedId4Data(ChunkSection lastSection, int x, int y, int z) {
        DataPaletteBlock dataPalette = lastSection.getBlocks();
        IBlockData ibd = dataPalette.a(x & 15, y & 15, z & 15);
        Block block = ibd.getBlock();
        int id = Block.getId(block);
        if (FaweCache.hasData(id)) {
            return (id << 4) + block.toLegacyData(ibd);
        } else {
            return id << 4;
        }
    }

    @Override
    public int getBiome(net.minecraft.server.v1_12_R1.Chunk chunk, int x, int z) {
        return chunk.getBiomeIndex()[((z & 15) << 4) + (x & 15)];
    }

    @Override
    public int getOpacity(ChunkSection section, int x, int y, int z) {
        DataPaletteBlock dataPalette = section.getBlocks();
        IBlockData ibd = dataPalette.a(x & 15, y & 15, z & 15);
        return ibd.c();
    }

    @Override
    public int getBrightness(ChunkSection section, int x, int y, int z) {
        DataPaletteBlock dataPalette = section.getBlocks();
        IBlockData ibd = dataPalette.a(x & 15, y & 15, z & 15);
        return ibd.d();
    }

    @Override
    public int getOpacityBrightnessPair(ChunkSection section, int x, int y, int z) {
        DataPaletteBlock dataPalette = section.getBlocks();
        IBlockData ibd = dataPalette.a(x & 15, y & 15, z & 15);
        return MathMan.pair16(ibd.c(), ibd.d());
    }

    @Override
    public void sendChunk(int x, int z, int bitMask) {
        net.minecraft.server.v1_12_R1.Chunk chunk = getCachedChunk(getWorld(), x, z);
        if (chunk != null) {
            sendChunk(getPlayerChunk((WorldServer) chunk.getWorld(), chunk.locX, chunk.locZ), chunk, bitMask);
        }
    }

    @Override
    public void sendBlockUpdate(FaweChunk chunk, FawePlayer... players) {
        try {
            PlayerChunkMap playerManager = ((CraftWorld) getWorld()).getHandle().getPlayerChunkMap();
            boolean watching = false;
            boolean[] watchingArr = new boolean[players.length];
            for (int i = 0; i < players.length; i++) {
                EntityPlayer player = ((CraftPlayer) ((BukkitPlayer) players[i]).parent).getHandle();
                if (playerManager.a(player, chunk.getX(), chunk.getZ())) {
                    watchingArr[i] = true;
                    watching = true;
                }
            }
            if (!watching) return;
            final LongAdder size = new LongAdder();
            if (chunk instanceof VisualChunk) {
                size.add(((VisualChunk) chunk).size());
            } else if (chunk instanceof CharFaweChunk) {
                size.add(((CharFaweChunk) chunk).getTotalCount());
            } else {
                chunk.forEachQueuedBlock(new FaweChunkVisitor() {
                    @Override
                    public void run(int localX, int y, int localZ, int combined) {
                        size.add(1);
                    }
                });
            }
            if (size.intValue() == 0) return;
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
            final PacketDataSerializer buffer = new PacketDataSerializer(byteBuf);
            buffer.writeInt(chunk.getX());
            buffer.writeInt(chunk.getZ());
            buffer.d(size.intValue());
            chunk.forEachQueuedBlock(new FaweChunkVisitor() {
                @Override
                public void run(int localX, int y, int localZ, int combined) {
                    short index = (short) (localX << 12 | localZ << 8 | y);
                    if (combined < 16) combined = 0;
                    buffer.writeShort(index);
                    buffer.d(combined);
                }
            });
            packet.a(buffer);
            for (int i = 0; i < players.length; i++) {
                if (watchingArr[i]) ((CraftPlayer) ((BukkitPlayer) players[i]).parent).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshChunk(FaweChunk fc) {
        sendChunk(fc.getX(), fc.getZ(), fc.getBitMask());
    }

    private PlayerChunk getPlayerChunk(WorldServer w, int cx, int cz) {
        PlayerChunkMap chunkMap = w.getPlayerChunkMap();
        PlayerChunk playerChunk = chunkMap.getChunk(cx, cz);
        if (playerChunk == null) {
            return null;
        }
        if (playerChunk.c.isEmpty()) {
            return null;
        }
        return playerChunk;
    }

    public boolean sendChunk(PlayerChunk playerChunk, net.minecraft.server.v1_12_R1.Chunk nmsChunk, int mask) {
        WorldServer w = (WorldServer) nmsChunk.getWorld();
        if (playerChunk == null) {
            return false;
        }
        if (mask == 0) {
            PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(nmsChunk, 65535);
            for (EntityPlayer player : playerChunk.c) {
                player.playerConnection.sendPacket(packet);
            }
            return true;
        }
        // Send chunks
        boolean empty = false;
        ChunkSection[] sections = nmsChunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            if (sections[i] == null) {
                sections[i] = emptySection;
                empty = true;
            }
        }
        if (mask == 0 || mask == 65535 && hasEntities(nmsChunk)) {
            PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(nmsChunk, 65280);
            for (EntityPlayer player : playerChunk.c) {
                player.playerConnection.sendPacket(packet);
            }
            mask = 255;
        }
        PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(nmsChunk, mask);
        for (EntityPlayer player : playerChunk.c) {
            player.playerConnection.sendPacket(packet);
        }
        if (empty) {
            for (int i = 0; i < sections.length; i++) {
                if (sections[i] == emptySection) {
                    sections[i] = null;
                }
            }
        }
        return true;
    }

    public boolean hasEntities(net.minecraft.server.v1_12_R1.Chunk nmsChunk) {
        try {
            final Collection<Entity>[] entities = (Collection<Entity>[]) getEntitySlices.invoke(nmsChunk);
            for (int i = 0; i < entities.length; i++) {
                Collection<Entity> slice = entities[i];
                if (slice != null && !slice.isEmpty()) {
                    return true;
                }
            }
        } catch (Throwable ignore) {}
        return false;
    }

    @Override
    public boolean removeLighting(ChunkSection[] sections, RelightMode mode, boolean sky) {
        if (mode != RelightMode.NONE) {
            for (int i = 0; i < sections.length; i++) {
                ChunkSection section = sections[i];
                if (section != null) {
                    section.a(new NibbleArray()); // Emitted
                    if (sky) {
                        section.b(new NibbleArray()); // Skylight
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void setFullbright(ChunkSection[] sections) {
        for (int i = 0; i < sections.length; i++) {
            ChunkSection section = sections[i];
            if (section != null) {
                byte[] bytes = section.getSkyLightArray().asBytes();
                Arrays.fill(bytes, (byte) 255);
            }
        }
    }

    @Override
    public int getSkyLight(ChunkSection section, int x, int y, int z) {
        return section.b(x & 15, y & 15, z & 15);
    }

    @Override
    public int getEmmittedLight(ChunkSection section, int x, int y, int z) {
        return section.c(x & 15, y & 15, z & 15);
    }

    @Override
    public void relightBlock(int x, int y, int z) {
        pos.c(x, y, z);
        nmsWorld.c(EnumSkyBlock.BLOCK, pos);
    }

    @Override
    public void relightSky(int x, int y, int z) {
        pos.c(x, y, z);
        nmsWorld.c(EnumSkyBlock.SKY, pos);
    }

    @Override
    public void relight(int x, int y, int z) {
        pos.c(x, y, z);
        nmsWorld.w(pos);
    }

    protected WorldServer nmsWorld;

    @Override
    public World getImpWorld() {
        World world = super.getImpWorld();
        if (world != null) {
            this.nmsWorld = ((CraftWorld) world).getHandle();
            return super.getImpWorld();
        } else {
            return null;
        }
    }

    public void setCount(int tickingBlockCount, int nonEmptyBlockCount, ChunkSection section) throws NoSuchFieldException, IllegalAccessException {
        fieldTickingBlockCount.set(section, tickingBlockCount);
        fieldNonEmptyBlockCount.set(section, nonEmptyBlockCount);
    }

    public int getNonEmptyBlockCount(ChunkSection section) throws IllegalAccessException {
        return (int) fieldNonEmptyBlockCount.get(section);
    }

    public void setPalette(ChunkSection section, DataPaletteBlock palette) throws NoSuchFieldException, IllegalAccessException {
        fieldSection.set(section, palette);
        Arrays.fill(section.getEmittedLightArray().asBytes(), (byte) 0);
    }

    public ChunkSection newChunkSection(int y2, boolean flag, char[] array) {
        try {
            if (array == null) {
                return new ChunkSection(y2, flag);
            } else {
                return new ChunkSection(y2, flag, array);
            }
        } catch (Throwable e) {
            try {
                if (array == null) {
                    Constructor<ChunkSection> constructor = ChunkSection.class.getDeclaredConstructor(int.class, boolean.class, IBlockData[].class);
                    return constructor.newInstance(y2, flag, (IBlockData[]) null);
                } else {
                    Constructor<ChunkSection> constructor = ChunkSection.class.getDeclaredConstructor(int.class, boolean.class, char[].class, IBlockData[].class);
                    return constructor.newInstance(y2, flag, array, (IBlockData[]) null);
                }
            } catch (Throwable e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    @Override
    public BukkitChunk_1_12 getPrevious(CharFaweChunk fs, ChunkSection[] sections, Map<?, ?> tilesGeneric, Collection<?>[] entitiesGeneric, Set<UUID> createdEntities, boolean all) throws Exception {
        Map<BlockPosition, TileEntity> tiles = (Map<BlockPosition, TileEntity>) tilesGeneric;
        Collection<Entity>[] entities = (Collection<Entity>[]) entitiesGeneric;
        // Copy blocks
        BukkitChunk_1_12_Copy previous = new BukkitChunk_1_12_Copy(this, fs.getX(), fs.getZ());
        for (int layer = 0; layer < sections.length; layer++) {
            if (fs.getCount(layer) != 0 || all) {
                ChunkSection section = sections[layer];
                if (section != null) {
                    DataPaletteBlock blocks = section.getBlocks();
                    byte[] ids = new byte[4096];
                    NibbleArray data = new NibbleArray();
                    blocks.exportData(ids, data);
                    previous.set(layer, ids, data.asBytes());
                    short solid = (short) fieldNonEmptyBlockCount.getInt(section);
                    previous.count[layer] = solid;
                    previous.air[layer] = (short) (4096 - solid);
                }
            }
        }
        // Copy tiles
        if (tiles != null) {
            for (Map.Entry<BlockPosition, TileEntity> entry : tiles.entrySet()) {
                TileEntity tile = entry.getValue();
                NBTTagCompound tag = new NBTTagCompound();
                BlockPosition pos = entry.getKey();
                CompoundTag nativeTag = getTag(tile);
                previous.setTile(pos.getX() & 15, pos.getY(), pos.getZ() & 15, nativeTag);
            }
        }
        // Copy entities
        if (entities != null) {
            for (Collection<Entity> entityList : entities) {
                for (Entity ent : entityList) {
                    if (ent instanceof EntityPlayer || (!createdEntities.isEmpty() && createdEntities.contains(ent.getUniqueID()))) {
                        continue;
                    }
                    int x = ((int) Math.round(ent.locX) & 15);
                    int z = ((int) Math.round(ent.locZ) & 15);
                    int y = ((int) Math.round(ent.locY) & 0xFF);
                    int i = FaweCache.CACHE_I[y][z][x];
                    char[] array = fs.getIdArray(i);
                    if (array == null) {
                        continue;
                    }
                    int j = FaweCache.CACHE_J[y][z][x];
                    if (array[j] != 0) {
                        String id = EntityTypes.b(ent);
                        if (id != null) {
                            NBTTagCompound tag = new NBTTagCompound();
                            ent.save(tag); // readEntityIntoTag
                            CompoundTag nativeTag = (CompoundTag) methodToNative.invoke(adapter, tag);
                            Map<String, Tag> map = ReflectionUtils.getMap(nativeTag.getValue());
                            map.put("Id", new StringTag(id));
                            previous.setEntity(nativeTag);
                        }
                    }
                }
            }
        }
        return previous;
    }

    protected BlockPosition.MutableBlockPosition pos = new BlockPosition.MutableBlockPosition(0, 0, 0);

    @Override
    public CompoundTag getTileEntity(net.minecraft.server.v1_12_R1.Chunk chunk, int x, int y, int z) {
        Map<BlockPosition, TileEntity> tiles = chunk.getTileEntities();
        pos.c(x, y, z);
        TileEntity tile = tiles.get(pos);
        return tile != null ? getTag(tile) : null;
    }

    public CompoundTag getTag(TileEntity tile) {
        try {
            NBTTagCompound tag = new NBTTagCompound();
            tile.save(tag); // readTagIntoEntity
            return (CompoundTag) methodToNative.invoke(adapter, tag);
        } catch (Exception e) {
            MainUtil.handleError(e);
            return null;
        }
    }

    @Deprecated
    public boolean unloadChunk(final String world, final Chunk chunk) {
        net.minecraft.server.v1_12_R1.Chunk c = ((CraftChunk) chunk).getHandle();
        c.mustSave = false;
        if (chunk.isLoaded()) {
            chunk.unload(false, false);
        }
        return true;
    }

    @Override
    public BukkitChunk_1_12 getFaweChunk(int x, int z) {
        return new BukkitChunk_1_12(this, x, z);
    }
}
