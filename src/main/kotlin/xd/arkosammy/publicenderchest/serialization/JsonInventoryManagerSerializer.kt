package xd.arkosammy.publicenderchest.serialization

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import net.minecraft.server.MinecraftServer
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.getModFolderPath
import xd.arkosammy.publicenderchest.inventory.InventoryManager
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class JsonInventoryManagerSerializer<T : InventoryManager>(override val inventoryManager: T) : InventoryManagerSerializer<T> {

    override fun writeManager(codec: Codec<in T>, fileName: String, server: MinecraftServer, logWrite: Boolean) {
        val filePath: Path = getModFolderPath(server).resolve(fileName + FILE_SUFFIX)
        val encodedManager: DataResult<JsonElement> = codec.encodeStart(JsonOps.COMPRESSED, inventoryManager)
        val encodedJson: JsonElement = encodedManager.getOrThrow { e ->
            throw IllegalStateException("Error attempting to encode inventory manager: $e")
        }
        val gson = Gson()
        val jsonString: String = gson.toJson(encodedJson)
        Files.newBufferedWriter(filePath).use { bw ->
            bw.write(jsonString)
            if (logWrite) {
                PublicEnderChest.LOGGER.info("Stored inventory manager to: $filePath")
            }
        }
    }

    override fun readManager(codec: Codec<out T>, fileName: String, server: MinecraftServer): T? {
        val filePath: Path = getModFolderPath(server).resolve(fileName + FILE_SUFFIX)
        try {
            if (!Files.exists(filePath)) {
                PublicEnderChest.LOGGER.warn("Public ender chest file not found! Creating new one at $filePath")
                Files.createDirectory(getModFolderPath(server))
                Files.createFile(filePath)
                return null
            }
            Files.newBufferedReader(filePath).use { br ->
                val jsonElement: JsonElement = JsonParser.parseReader(br)
                val decodedManager: DataResult<out T> = codec.parse(JsonOps.COMPRESSED, jsonElement)
                val optionalManager: Optional<out T> = decodedManager.resultOrPartial { e -> PublicEnderChest.LOGGER.error("Error reading public inventory manager file $filePath : $e") }
                return optionalManager.get()
            }
        } catch (e: Exception) {
            PublicEnderChest.LOGGER.error("Error reading public inventory manager file $filePath : $e")
            return null
        }
    }

    companion object {

        private const val FILE_SUFFIX = ".json"

    }

}