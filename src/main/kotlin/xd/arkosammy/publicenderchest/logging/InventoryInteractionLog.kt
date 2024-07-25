package xd.arkosammy.publicenderchest.logging

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier
import xd.arkosammy.publicenderchest.PublicEnderChest
import java.sql.Connection
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed interface InventoryInteractionLog {

    val playerName: String

    val uuid: String

    val itemStackId: Identifier

    val quantity: Int

    val timestamp: LocalDateTime

    fun getLogText() : MutableText

    fun consumeDbConnection(connection: Connection)

    companion object {

        fun of(logActionType: InventoryInteractionType, player: ServerPlayerEntity, itemStack: ItemStack, quantity: Int) : InventoryInteractionLog {
            val now: LocalDateTime = LocalDateTime.now()
            val playerName: String = player.name.string
            val uuid: String = player.uuid.toString()
            val item: Item = itemStack.item
            val itemStackRegistryKey: RegistryKey<Item>? = Registries.ITEM.getKey(item).orElse(null)
            val itemStackId: Identifier = if (itemStackRegistryKey == null) {
                PublicEnderChest.LOGGER.error("Error logging Public Ender Chest interaction: Unknown Item \"$item\"")
                Identifier.ofVanilla("unknown_item")
            } else {
                itemStackRegistryKey.value
            }
            return when (logActionType) {
                InventoryInteractionType.ITEM_REMOVE -> ItemRemoveLog(playerName, uuid, itemStackId, quantity, now)
                InventoryInteractionType.ITEM_INSERT -> ItemInsertLog(playerName, uuid, itemStackId, quantity, now)
            }
        }

        fun formatElapsedTime(duration: Duration) : String {
            val days: Long = duration.toDays()
            val hours: Long = duration.toHours() % 24
            val minutes: Long = duration.toMinutes() % 60
            val seconds: Long = duration.toSeconds() % 60
            var result = "$seconds second(s) ago"
            if (minutes > 0) {
               result = "$minutes minute(s) ago"
            }
            if (hours > 0) {
                result = "$hours hour(s) ago"
            }
            if (days > 0) {
                result = "$days day(s) ago"
            }
            return result
        }

        val DTF: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm:ss a")

    }

}