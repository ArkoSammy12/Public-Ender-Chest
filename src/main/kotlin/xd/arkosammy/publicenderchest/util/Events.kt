package xd.arkosammy.publicenderchest.util

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.commands.CommandManager
import xd.arkosammy.publicenderchest.networking.OpenPublicInventoryPayload
import xd.arkosammy.publicenderchest.util.ducks.ServerPlayerEntityDuck

object Events {

    fun registerEvents() {
        CommandRegistrationCallback.EVENT.register(CommandManager::registerCommands)
        UseBlockCallback.EVENT.register(::onBlockInteracted)
        UseItemCallback.EVENT.register(::onItemInteracted)
        ServerTickEvents.END_SERVER_TICK.register(::onServerTick)
        PayloadTypeRegistry.playC2S().register(OpenPublicInventoryPayload.PACKET_ID, OpenPublicInventoryPayload.PACKET_CODEC)
        ServerPlayNetworking.registerGlobalReceiver(OpenPublicInventoryPayload.PACKET_ID, ::handleOpenInventoryPayload)
    }

    private fun onServerTick(server: MinecraftServer) {
        PublicEnderChest.INVENTORY_MANAGER.tick(server)
    }

    private fun <T : CustomPayload> handleOpenInventoryPayload(payload: T, context: Context) {
        val player: ServerPlayerEntity = context.player()
        val isUsingPublicInventory: Boolean = player.getAttached(PublicEnderChest.USING_PUBLIC_INVENTORY) ?: true
        if (!isUsingPublicInventory) {
            return
        }
        if (!PublicEnderChest.INVENTORY_MANAGER.publicInventory.canPlayerUse(player)) {
            return
        }
        (player as ServerPlayerEntityDuck).`publicenderchest$openInventory`("Public Inventory", PublicEnderChest.INVENTORY_MANAGER.publicInventory)
    }

    private fun onBlockInteracted(player: PlayerEntity, world: World, hand: Hand, hitResult: BlockHitResult) : ActionResult {
        return PublicEnderChest.INVENTORY_MANAGER.onBlockInteractedListener(player, world, hand, hitResult)
    }

    private fun onItemInteracted(player: PlayerEntity, world: World, hand: Hand): TypedActionResult<ItemStack> {
        return PublicEnderChest.INVENTORY_MANAGER.onItemInteractedListener(player, world, hand)
    }

}