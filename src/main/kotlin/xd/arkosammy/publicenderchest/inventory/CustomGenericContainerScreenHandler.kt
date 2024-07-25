package xd.arkosammy.publicenderchest.inventory

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.logging.InventoryInteractionLog
import xd.arkosammy.publicenderchest.logging.InventoryInteractionType

class CustomGenericContainerScreenHandler(
    type: ScreenHandlerType<*>,
    syncId: Int,
    playerInventory: PlayerInventory,
    inventory: Inventory,
    rows: Int
) : GenericContainerScreenHandler(type, syncId, playerInventory, inventory, rows) {

    override fun quickMove(player: PlayerEntity, slotNumber: Int): ItemStack {
        if (player !is ServerPlayerEntity) {
            return super.quickMove(player, slotNumber)
        }
        val currentSlot: Slot = this.slots[slotNumber]
        if (currentSlot.inventory !is PublicInventory) {
            return super.quickMove(player, slotNumber)
        }
        val currentStack: ItemStack = currentSlot.stack
        if (currentStack.isEmpty) {
            return super.quickMove(player, slotNumber)
        }
        val serverPlayerEntity: ServerPlayerEntity = player as? ServerPlayerEntity ?: return super.quickMove(player, slotNumber)
        val itemRemoveLog: InventoryInteractionLog = InventoryInteractionLog.of(InventoryInteractionType.ITEM_REMOVE, serverPlayerEntity, currentStack.copy(), currentStack.count)
        PublicEnderChest.DATABASE_MANAGER.logInventoryInteraction(itemRemoveLog, serverPlayerEntity.server)
        return super.quickMove(player, slotNumber)
    }

}