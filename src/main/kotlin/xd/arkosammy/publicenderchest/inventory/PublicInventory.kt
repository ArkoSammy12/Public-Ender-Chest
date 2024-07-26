package xd.arkosammy.publicenderchest.inventory

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.component.ComponentMap
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.collection.DefaultedList
import xd.arkosammy.monkeyconfig.managers.getSettingValue
import xd.arkosammy.monkeyconfig.settings.BooleanSetting
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.config.ConfigSettings
import xd.arkosammy.publicenderchest.logging.InventoryInteractionLog
import xd.arkosammy.publicenderchest.logging.InventoryInteractionType
import xd.arkosammy.publicenderchest.serialization.SerializedItemStack
import kotlin.math.abs

class PublicInventory(private val itemStacks: DefaultedList<ItemStack> = DefaultedList.ofSize(SLOT_SIZE, ItemStack.EMPTY)) : Inventory {

    var dirty: Boolean = false
        private set
        get() {
            val currentValue: Boolean = field
            field = false
            return currentValue
        }

    private var currentPlayerHandler: ServerPlayerEntity? = null
    private var previousItemStack: DefaultedList<ItemStack> = DefaultedList.copyOf(ItemStack.EMPTY, *this.itemStacks.map { stack -> stack.copy() }.toTypedArray())

    override fun clear() {
        this.itemStacks.clear()
    }

    override fun size(): Int = this.itemStacks.size

    override fun isEmpty(): Boolean {
        for (stack: ItemStack in this.itemStacks) {
            if (!itemStacks.isEmpty()) {
                return false
            }
        }
        return true
    }

    override fun onOpen(player: PlayerEntity?) {
        super.onOpen(player)
        this.currentPlayerHandler = player as? ServerPlayerEntity
    }

    override fun onClose(player: PlayerEntity?) {
        super.onClose(player)
        this.currentPlayerHandler = null
    }

    override fun getStack(slot: Int): ItemStack =
        if (slot in (0 until this.size())) this.itemStacks[slot] else ItemStack.EMPTY

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(this.itemStacks, slot, amount)

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.itemStacks, slot)

    override fun setStack(slot: Int, stack: ItemStack) {
        this.itemStacks[slot] = stack
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        if (player !is ServerPlayerEntity) {
            return false
        }
        if (player.hasPermissionLevel(4)) {
            return true
        }
        val blackListEnabled: Boolean = PublicEnderChest.CONFIG_MANAGER.getSettingValue<Boolean, BooleanSetting>(ConfigSettings.ENABLE_PLAYER_BLACKLIST.settingLocation)
        val playerBlacklist: List<String> = PublicEnderChest.CONFIG_MANAGER.getSettingValue<List<String>, StringListSetting>(ConfigSettings.PLAYER_BLACKLIST.settingLocation)
        if (blackListEnabled && playerBlacklist.contains(player.name.string)) {
            return false
        }
        val dimensionBlackListEnabled = PublicEnderChest.CONFIG_MANAGER.getSettingValue<Boolean, BooleanSetting>(ConfigSettings.ENABLE_DIMENSION_BLACKLIST.settingLocation)
        val dimensionBlackList: List<String> = PublicEnderChest.CONFIG_MANAGER.getSettingValue<List<String>, StringListSetting>(ConfigSettings.DIMENSION_BLACKLIST.settingLocation)
        val currentWorld: String = player.world.registryKey.value.toString()
        if (dimensionBlackListEnabled && dimensionBlackList.contains(currentWorld)) {
            return false
        }
        return true
    }

    override fun markDirty() {
        this.dirty = true

        val player: ServerPlayerEntity? = this.currentPlayerHandler
        if (player == null) {
            this.updatePreviousStacks()
            return
        }

        for (i in 0 until this.itemStacks.size) {

            val previousStack: ItemStack = this.previousItemStack[i]
            val currentStack: ItemStack = this.itemStacks[i]

            // If the item stacks are of the same item, and
            // have the same components and count, then this slot remained unchanged
            if (ItemStack.areEqual(previousStack, currentStack)) {
                continue
            }

            // If the item stacks are of the same item and have the same components,
            // but are different in count, then log based on the count difference
            if (ItemStack.areItemsAndComponentsEqual(previousStack,  currentStack)) {
                val countDifference: Int = currentStack.count - previousStack.count
                if (countDifference > 0) {
                    val insertAction: InventoryInteractionLog = InventoryInteractionLog.of(InventoryInteractionType.ITEM_INSERT, player, currentStack.copy(), abs(countDifference))
                    PublicEnderChest.DATABASE_MANAGER.logInventoryInteraction(insertAction, player.server)
                } else if (countDifference < 0) {
                    val removeAction: InventoryInteractionLog = InventoryInteractionLog.of(InventoryInteractionType.ITEM_REMOVE, player, currentStack.copy(), abs(countDifference))
                    PublicEnderChest.DATABASE_MANAGER.logInventoryInteraction(removeAction, player.server)
                }
                continue
            }

            // If the item stacks differ in components and count,
            // then the item stacks cannot be combined,
            // so a complete replacement of the stack has occurred in this slot.
            // Log a removal and insertion based on the previous and current items stacks for the slot.
            if (!previousStack.isEmpty) {
                val removeAction: InventoryInteractionLog = InventoryInteractionLog.of(InventoryInteractionType.ITEM_REMOVE, player, previousStack.copy(), previousStack.count)
                PublicEnderChest.DATABASE_MANAGER.logInventoryInteraction(removeAction, player.server)
            }
            if (!currentStack.isEmpty) {
                val insertAction: InventoryInteractionLog = InventoryInteractionLog.of(InventoryInteractionType.ITEM_INSERT, player, currentStack.copy(), currentStack.count)
                PublicEnderChest.DATABASE_MANAGER.logInventoryInteraction(insertAction, player.server)
            }

        }
        this.updatePreviousStacks()
    }

    private fun updatePreviousStacks() {
        this.previousItemStack = DefaultedList.copyOf(ItemStack.EMPTY, *this.itemStacks.map { stack -> stack.copy() }.toTypedArray())
    }

    companion object {

        const val SLOT_SIZE = 54

        val CODEC: Codec<PublicInventory> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.list(SerializedItemStack.CODEC).fieldOf("item_stacks").forGetter { publicInventory ->
                    val serializedItemStacks: MutableList<SerializedItemStack> = mutableListOf()
                    for(i in 0 until publicInventory.size()) {
                        val currentStack = publicInventory.itemStacks[i]
                        if (currentStack.isEmpty) {
                            continue
                        }
                        serializedItemStacks.add(SerializedItemStack(i, currentStack))
                    }
                    return@forGetter serializedItemStacks
                }
            ).apply(instance) { serializedItemStacks ->
                val deserializedItemStacks: DefaultedList<ItemStack> = DefaultedList.ofSize(SLOT_SIZE, ItemStack.EMPTY)
                for (indexedStack: SerializedItemStack in serializedItemStacks) {
                    deserializedItemStacks[indexedStack.slotIndex] = indexedStack.itemStack
                }
                PublicInventory(DefaultedList.copyOf(ItemStack.EMPTY, *deserializedItemStacks.toTypedArray()))
            }
        }

    }

}