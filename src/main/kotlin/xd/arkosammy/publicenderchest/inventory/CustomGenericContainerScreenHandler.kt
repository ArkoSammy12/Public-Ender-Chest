package xd.arkosammy.publicenderchest.inventory

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType

class CustomGenericContainerScreenHandler(
    type: ScreenHandlerType<*>,
    syncId: Int,
    playerInventory: PlayerInventory,
    inventory: Inventory,
    rows: Int
) : GenericContainerScreenHandler(type, syncId, playerInventory, inventory, rows) {

    override fun quickMove(player: PlayerEntity, slotNumber: Int): ItemStack {
        return super.quickMove(player, slotNumber)
    }

}