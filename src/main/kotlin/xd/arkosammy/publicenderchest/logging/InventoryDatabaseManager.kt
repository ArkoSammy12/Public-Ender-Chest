package xd.arkosammy.publicenderchest.logging

import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.getModFolderPath
import java.sql.DriverManager
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

class InventoryDatabaseManager(server: MinecraftServer) {

    init {

        val tableInit = """
           CREATE TABLE IF NOT EXISTS $MAIN_TABLE_NAME (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            player TEXT,
            uuid TEXT,
            item TEXT,
            quantity INTEGER,
            timestamp TIMESTAMP,
            interaction_type TEXT
        );"""

        val url: String = getDatabaseFileUrl(server)

        DriverManager.getConnection(url).use { c ->
            try {
                Class.forName("org.sqlite.JDBC")
                val statement: Statement = c.createStatement()
                statement.execute(tableInit)
            } catch (e: Exception) {
                PublicEnderChest.LOGGER.error("Error initializing Public Ender Chest database: $e")
            }
        }

    }

    fun logInventoryInteraction(inventoryInteractionLog: InventoryInteractionLog, server: MinecraftServer) {
        val url: String = getDatabaseFileUrl(server)
        try {
            DriverManager.getConnection(url).use { c ->
                inventoryInteractionLog.consumeDbConnection(c)
            }
        } catch (e: Exception) {
            PublicEnderChest.LOGGER.error("Error attempting to log to Public Ender Chest database: $e")
        }
    }

    fun query(server: MinecraftServer, queryContext: QueryContext) : List<InventoryInteractionLog> {
        val url: String = getDatabaseFileUrl(server)
        val queryTimeStamp: Timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(queryContext.days.toLong()).minusHours(queryContext.hours.toLong()).minusMinutes(queryContext.minutes.toLong()).minusSeconds(queryContext.seconds.toLong()))
        val query: String = when (queryContext.timeQueryType) {
            TimeQueryType.BEFORE -> "SELECT * FROM ${MAIN_TABLE_NAME} WHERE timestamp < ?"
            TimeQueryType.AFTER -> "SELECT * FROM ${MAIN_TABLE_NAME} WHERE timestamp > ?"
        }

        val results: MutableList<InventoryInteractionLog> = mutableListOf()

        try {
            DriverManager.getConnection(url).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setTimestamp(1, queryTimeStamp)
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val playerName: String = resultSet.getString("player")
                            val uuid: String = resultSet.getString("uuid")
                            val itemString: String = resultSet.getString("item")
                            val quantity: Int = resultSet.getInt("quantity")
                            val timeStamp: LocalDateTime = resultSet.getTimestamp("timestamp").toLocalDateTime()
                            val interactionType: String = resultSet.getString("interaction_type")
                            val itemId: Identifier? = Identifier.tryParse(itemString)
                            if (itemId == null) {
                                PublicEnderChest.LOGGER.error("Could not retrieve database entry due to invalid Item identifier: \"${itemString}\"")
                                continue
                            }
                            val inventoryInteractionLog: InventoryInteractionLog = when (interactionType) {
                                InventoryInteractionType.ITEM_REMOVE.asString() -> ItemRemoveLog(playerName, uuid, itemId, quantity, timeStamp)
                                InventoryInteractionType.ITEM_INSERT.asString() -> ItemInsertLog(playerName, uuid, itemId, quantity, timeStamp)
                                else -> continue
                            }
                            results.add(inventoryInteractionLog)
                        }

                    }

                }

            }
        } catch (e: Exception) {
            PublicEnderChest.LOGGER.error("Error attempting to query from Public Ender Chest database: $e")
        }
        return results.toList()
    }

    fun purge(server: MinecraftServer, daysBefore: Int) : Int {
        var deletedRows: Int
        val url: String = getDatabaseFileUrl(server)
        DriverManager.getConnection(url).use { connection ->
            connection.prepareStatement("DELETE FROM $MAIN_TABLE_NAME WHERE timestamp < ?").use { statement ->
                val timestamp: Timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(daysBefore.toLong()))
                statement.setTimestamp(1, timestamp)
                deletedRows = statement.executeUpdate()
            }
        }
        PublicEnderChest.LOGGER.info("Purged $deletedRows entries from Public Ender Chest inventory log database")
        return deletedRows
    }


    companion object {

        const val MAIN_TABLE_NAME = "public_inventory_interactions"
        private const val DATABASE_FILE_NAME = "${PublicEnderChest.MOD_ID}.db"

        fun getDatabaseFileUrl(server: MinecraftServer) : String =
            "jdbc:sqlite:${getModFolderPath(server).resolve(DATABASE_FILE_NAME)}"

    }



}

