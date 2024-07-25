package xd.arkosammy.publicenderchest.logging

import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.sql.Connection
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime

class ItemRemoveLog(
    override val playerName: String,
    override val uuid: String,
    override val itemStackId: Identifier,
    override val quantity: Int,
    override val timestamp: LocalDateTime
) : InventoryInteractionLog {

    override fun getLogText(): MutableText {
        val duration: Duration = Duration.between(timestamp, LocalDateTime.now())
        val durationText: MutableText = Text.literal("${InventoryInteractionLog.formatElapsedTime(duration)} ")
            .setStyle(Style.EMPTY.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(timestamp.format(InventoryInteractionLog.DTF)))))
            .formatted(Formatting.DARK_AQUA)
        val playerText: MutableText = Text.literal("$playerName ")
            .setStyle(Style.EMPTY.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(uuid))).withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid)))
            .formatted(Formatting.AQUA)
        val interactedInventoryText: MutableText = Text.literal("removed ")
            .formatted(Formatting.YELLOW)
        val quantityText: MutableText = Text.literal("$quantity ")
            .formatted(Formatting.BLUE)
        val itemText: MutableText = Text.literal("$itemStackId")
            .formatted(Formatting.GREEN)

        return Text.empty().append(durationText).append(playerText).append(interactedInventoryText).append(quantityText).append(itemText)
    }

    override fun consumeDbConnection(connection: Connection) {
        connection.prepareStatement("INSERT INTO ${InventoryDatabaseManager.MAIN_TABLE_NAME} (player, uuid, item, quantity, timestamp, interaction_type) VALUES (?, ?, ?, ?, ?, ?)").use { statement ->
            statement.setString(1, playerName)
            statement.setString(2, uuid)
            statement.setString(3, itemStackId.toString())
            statement.setInt(4, quantity)
            statement.setTimestamp(5, Timestamp.valueOf(timestamp))
            statement.setString(6, InventoryInteractionType.ITEM_REMOVE.asString())
            statement.executeUpdate()
        }
    }

}