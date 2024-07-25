package xd.arkosammy.publicenderchest.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtSizeTracker
import net.minecraft.server.MinecraftServer
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.getModFolderPath
import xd.arkosammy.publicenderchest.inventory.InventoryManager
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class NbtInventoryManagerSerializer<T : InventoryManager>(override val inventoryManager: T) : InventoryManagerSerializer<T> {

    override fun writeManager(codec: Codec<in T>, fileName: String, server: MinecraftServer, logWrite: Boolean) {
        val filePath: Path = getModFolderPath(server).resolve(getPathNameForFile(fileName))
        val encodedManager: DataResult<NbtElement> = codec.encodeStart(NbtOps.INSTANCE, inventoryManager)
        val encodedNbt: NbtElement = encodedManager.getOrThrow { e ->
            throw IllegalStateException("Error attempting to encode inventory manager: $e")
        }
        if (encodedNbt !is NbtCompound) {
            throw IllegalStateException("Error attempting to encode inventory manager: Encoded nbt is not an NbtCompound!")
        }
        try {
            NbtIo.writeCompressed(encodedNbt, filePath)
            if (logWrite) {
                PublicEnderChest.LOGGER.info("Stored inventory manager to: $filePath")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Error attempting to encode inventory manager: $e")
        }
    }

    override fun readManager(codec: Codec<out T>, fileName: String, server: MinecraftServer): T? {
        val filePath: Path = getModFolderPath(server).resolve(getPathNameForFile(fileName))
        try {
            if (!Files.exists(filePath)) {
                PublicEnderChest.LOGGER.warn("Public ender chest file not found! Creating new one at $filePath")
                Files.createDirectory(getModFolderPath(server))
                Files.createFile(filePath)
                return null
            }
            val nbtCompound: NbtCompound = NbtIo.readCompressed(filePath, NbtSizeTracker.ofUnlimitedBytes())
            val decodedManager: DataResult<out T> = codec.parse(NbtOps.INSTANCE, nbtCompound)
            val optionalManager: Optional<out T> = decodedManager.resultOrPartial { e -> PublicEnderChest.LOGGER.error("Error reading public inventory manager file $filePath : $e") }
            return optionalManager.get()
        } catch (e: Exception) {
            PublicEnderChest.LOGGER.error("Error reading public inventory manager file $filePath : $e")
            return null
        }
    }

    companion object {

        fun getPathNameForFile(fileName: String) : String = "$fileName.nbt"

    }

}