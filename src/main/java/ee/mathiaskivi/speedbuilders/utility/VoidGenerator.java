package ee.mathiaskivi.speedbuilders.utility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, final BiomeGrid biomeGrid) {
		final ChunkData chunkData = createChunkData(world);

		if (0 >= chunkX << 4 && 0 < chunkX + 1 << 4 && 0 >= chunkZ << 4 && 0 < chunkZ + 1 << 4) {
			chunkData.setBlock(0, 0, 0, Material.BEDROCK);
		}

		return chunkData;
	}

	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0.5, 1, 0.5);
	}
}
