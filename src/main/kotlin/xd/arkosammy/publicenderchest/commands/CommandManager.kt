package xd.arkosammy.publicenderchest.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.util.ducks.ServerPlayerEntityDuck

object CommandManager {

    fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>, registryAccess: CommandRegistryAccess, environment: CommandManager.RegistrationEnvironment) {
        if (environment != CommandManager.RegistrationEnvironment.DEDICATED) {
            return
        }
        val parentNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal(PublicEnderChest.MOD_ID + "-regular_commands")
            .build()

        val setTogglePublicInventoryUseNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal("usePublicInventory")
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.player ?: return@executes Command.SINGLE_SUCCESS
                val isUsingPublicInventory: Boolean = player.getAttached(PublicEnderChest.USING_PUBLIC_INVENTORY) ?: return@executes Command.SINGLE_SUCCESS
                val feedBackText: String = if (isUsingPublicInventory) "enabled" else "disabled"
                player.sendMessage(Text.literal("Usage of public ender chest inventory is currently $feedBackText"))
                Command.SINGLE_SUCCESS
            }
            .build()

        val togglePublicInventoryUseNodeArgumentNode: ArgumentCommandNode<ServerCommandSource, Boolean> = CommandManager
            .argument("toggle", BoolArgumentType.bool())
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.player ?: return@executes Command.SINGLE_SUCCESS
                val toggle: Boolean = BoolArgumentType.getBool(ctx, "toggle")
                player.setAttached(PublicEnderChest.USING_PUBLIC_INVENTORY, toggle)
                val feedBackText: String = if (toggle) "enabled" else "disabled"
                player.sendMessage(Text.literal("Usage of public ender chest inventory has been $feedBackText"))
                Command.SINGLE_SUCCESS
            }
            .build()

        val openPublicInventoryNode: LiteralCommandNode<ServerCommandSource> = CommandManager
            .literal("openPublicEnderChestInventory")
            .requires { src -> src.hasPermissionLevel(4) }
            .executes { ctx ->
                val player: ServerPlayerEntity = ctx.source.player ?: return@executes Command.SINGLE_SUCCESS
                (player as ServerPlayerEntityDuck).`publicenderchest$openInventory`("Public Inventory", PublicEnderChest.INVENTORY_MANAGER.publicInventory)
                Command.SINGLE_SUCCESS
            }
            .build()

        dispatcher.root.addChild(parentNode)
        parentNode.addChild(setTogglePublicInventoryUseNode)
        setTogglePublicInventoryUseNode.addChild(togglePublicInventoryUseNodeArgumentNode)
        parentNode.addChild(openPublicInventoryNode)


    }

}