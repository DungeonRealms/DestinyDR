package net.dungeonrealms.common.util;


import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class VoidChunkGenerator extends ChunkGenerator {

    public byte[] generate(World world, Random random, int x, int z) {
        return new byte[32768];
    }
}