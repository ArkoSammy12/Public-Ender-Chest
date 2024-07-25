package xd.arkosammy.publicenderchest.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import xd.arkosammy.monkeyconfig.managers.getSettingValue
import xd.arkosammy.monkeyconfig.settings.NumberSetting
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.config.ConfigSettings
import xd.arkosammy.publicenderchest.logging.QueryContext
import xd.arkosammy.publicenderchest.logging.TimeQueryType
import xd.arkosammy.publicenderchest.util.ducks.ServerPlayerEntityDuck

object DatabaseCommands {

    fun registerCommands(rootNode: CommandNode<ServerCommandSource>) {

        val databaseNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal("database")
            .requires { src -> src.hasPermissionLevel(4) }
            .build()

        val purgeNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal("purge")
            .requires { src -> src.hasPermissionLevel(4) }
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.playerOrThrow
                player.sendMessage(Text.literal("Initiating purge...").formatted(Formatting.GRAY))
                val deletedRows: Int = PublicEnderChest.DATABASE_MANAGER.purge(player.server, PublicEnderChest.CONFIG_MANAGER.getSettingValue<Int, NumberSetting<Int>>(ConfigSettings.PURGE_OLDER_THAN_X_DAYS.settingLocation))
                val deletedRowsText: MutableText = Text.literal("$deletedRows").formatted(Formatting.AQUA)
                player.sendMessage(Text.empty().append(Text.literal("Purged ")).append(deletedRowsText).append(Text.literal(" entries from the Public Ender Chest inventory database.")))
                Command.SINGLE_SUCCESS
            }
            .build()

        val pageNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal("page")
            .requires { src -> src.hasPermissionLevel(4) }
            .build()

        val pageArgumentNode: ArgumentCommandNode<ServerCommandSource, Int> = CommandManager
            .argument("page", IntegerArgumentType.integer())
            .requires { src -> src.hasPermissionLevel(4) }
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.playerOrThrow
                val newPageIndex: Int = IntegerArgumentType.getInteger(ctx, "page") - 1
                if (newPageIndex < 0 || newPageIndex > (player as ServerPlayerEntityDuck).`publicenderchest$getCachedLogs`().size - 1) {
                    player.sendMessage(Text.literal("No more pages to show!").formatted(Formatting.RED))
                    return@executes Command.SINGLE_SUCCESS
                }
                (player as ServerPlayerEntityDuck).`publicenderchest$setPageIndex`(newPageIndex)
                (player as ServerPlayerEntityDuck).`publicenderchest$showPage`()
                Command.SINGLE_SUCCESS
            }
            .build()

        val queryNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal("query")
            .requires { src -> src.hasPermissionLevel(4) }
            .build()

        val timeQueryTypeNode: ArgumentCommandNode<ServerCommandSource, String> = CommandManager
            .argument("timeQueryType", StringArgumentType.word())
            .suggests { _, suggestionBuilder ->
                CommandSource.suggestMatching(
                    TimeQueryType.entries.map { type -> type.commandNodeName },
                    suggestionBuilder
                )
            }
            .requires { src -> src.hasPermissionLevel(4) }
            .build()

        val queryWithDays: ArgumentCommandNode<ServerCommandSource, Int> = CommandManager
            .argument("days", IntegerArgumentType.integer(0))
            .requires { src -> src.hasPermissionLevel(4) }
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.playerOrThrow
                val queryTypeName: String = StringArgumentType.getString(ctx, "timeQueryType")
                val queryType: TimeQueryType = TimeQueryType.getFromCommandNodeName(queryTypeName) ?: return@executes Command.SINGLE_SUCCESS.also {
                    player.sendMessage(Text.literal("Time query type \"$queryTypeName\" does not exist!").formatted(Formatting.RED))
                }
                val days: Int = IntegerArgumentType.getInteger(ctx, "days")
                val queryContext = QueryContext(queryType, days)
                (player as ServerPlayerEntityDuck).`publicenderchest$showLogs`(queryContext)
                Command.SINGLE_SUCCESS
            }
            .build()

        val queryWithHours: ArgumentCommandNode<ServerCommandSource, Int> = CommandManager
            .argument("hours", IntegerArgumentType.integer(0))
            .requires { src -> src.hasPermissionLevel(4) }
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.playerOrThrow
                val queryTypeName: String = StringArgumentType.getString(ctx, "timeQueryType")
                val queryType: TimeQueryType = TimeQueryType.getFromCommandNodeName(queryTypeName) ?: return@executes Command.SINGLE_SUCCESS.also {
                    player.sendMessage(Text.literal("Time query type \"$queryTypeName\" does not exist!").formatted(Formatting.RED))
                }
                val days: Int = IntegerArgumentType.getInteger(ctx, "days")
                val hours: Int = IntegerArgumentType.getInteger(ctx, "hours")
                val queryContext = QueryContext(queryType, days, hours)
                (player as ServerPlayerEntityDuck).`publicenderchest$showLogs`(queryContext)
                Command.SINGLE_SUCCESS
            }
            .build()

        val queryWithMinutes: ArgumentCommandNode<ServerCommandSource, Int> = CommandManager
            .argument("minutes", IntegerArgumentType.integer(0))
            .requires { src -> src.hasPermissionLevel(4) }
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.playerOrThrow
                val queryTypeName: String = StringArgumentType.getString(ctx, "timeQueryType")
                val queryType: TimeQueryType = TimeQueryType.getFromCommandNodeName(queryTypeName) ?: return@executes Command.SINGLE_SUCCESS.also {
                    player.sendMessage(Text.literal("Time query type \"$queryTypeName\" does not exist!").formatted(Formatting.RED))
                }
                val days: Int = IntegerArgumentType.getInteger(ctx, "days")
                val hours: Int = IntegerArgumentType.getInteger(ctx, "hours")
                val minutes: Int = IntegerArgumentType.getInteger(ctx, "minutes")
                val queryContext = QueryContext(queryType, days, hours, minutes)
                (player as ServerPlayerEntityDuck).`publicenderchest$showLogs`(queryContext)
                Command.SINGLE_SUCCESS
            }
            .build()

        val queryWithSeconds: ArgumentCommandNode<ServerCommandSource, Int> = CommandManager
            .argument("seconds", IntegerArgumentType.integer(0))
            .requires { src -> src.hasPermissionLevel(4) }
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.playerOrThrow
                val queryTypeName: String = StringArgumentType.getString(ctx, "timeQueryType")
                val queryType: TimeQueryType = TimeQueryType.getFromCommandNodeName(queryTypeName) ?: return@executes Command.SINGLE_SUCCESS.also {
                    player.sendMessage(Text.literal("Time query type \"$queryTypeName\" does not exist!").formatted(Formatting.RED))
                }
                val days: Int = IntegerArgumentType.getInteger(ctx, "days")
                val hours: Int = IntegerArgumentType.getInteger(ctx, "hours")
                val minutes: Int = IntegerArgumentType.getInteger(ctx, "minutes")
                val seconds: Int = IntegerArgumentType.getInteger(ctx, "seconds")
                val queryContext = QueryContext(queryType, days, hours, minutes, seconds)
                (player as ServerPlayerEntityDuck).`publicenderchest$showLogs`(queryContext)
                Command.SINGLE_SUCCESS
            }
            .build()

        rootNode.addChild(databaseNode)

        databaseNode.addChild(purgeNode)

        databaseNode.addChild(queryNode)

        queryNode.addChild(pageNode)
        pageNode.addChild(pageArgumentNode)

        queryNode.addChild(timeQueryTypeNode)
        timeQueryTypeNode.addChild(queryWithDays)
        queryWithDays.addChild(queryWithHours)
        queryWithHours.addChild(queryWithMinutes)
        queryWithMinutes.addChild(queryWithSeconds)

    }

}