package xd.arkosammy.publicenderchest.inventory

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World
import xd.arkosammy.publicenderchest.PublicEnderChest
import xd.arkosammy.publicenderchest.isEnderChest
import xd.arkosammy.publicenderchest.serialization.InventoryManagerSerializer
import xd.arkosammy.publicenderchest.serialization.NbtInventoryManagerSerializer
import xd.arkosammy.publicenderchest.util.ducks.ServerPlayerEntityDuck

class PublicInventoryManager(inputPublicInventory: PublicInventory = PublicInventory()) : InventoryManager {

    var publicInventory: PublicInventory = inputPublicInventory
        private set

    private val serializer: InventoryManagerSerializer<PublicInventoryManager> = NbtInventoryManagerSerializer(this)

    override fun tick(server: MinecraftServer) {
        if (publicInventory.dirty) {
            serializer.writeManager(CODEC, PUBLIC_INVENTORY_FILE_NAME, server)
        }
    }

    override fun onServerStarting(server: MinecraftServer) {
        val inventoryManager: PublicInventoryManager = serializer.readManager(CODEC, PUBLIC_INVENTORY_FILE_NAME, server) ?: return
        this.publicInventory = inventoryManager.publicInventory
    }

    override fun onServerStopping(server: MinecraftServer) {
        serializer.writeManager(CODEC, PUBLIC_INVENTORY_FILE_NAME, server, true)
    }

    override fun onBlockInteractedListener(player: PlayerEntity, world: World, hand: Hand, hitResult: BlockHitResult): ActionResult {
        if (world.isClient() || player !is ServerPlayerEntity) {
            return ActionResult.PASS
        }
        val interactedBlockState: BlockState = world.getBlockState(hitResult.blockPos)
        if (!interactedBlockState.isEnderChest()) {
            return ActionResult.PASS
        }
        val isUsingPublicInventory: Boolean = player.getAttached(PublicEnderChest.USING_PUBLIC_INVENTORY) ?: true
        if (!isUsingPublicInventory) {
            return ActionResult.PASS
        }
        if (!player.isSneaking || player.isSpectator) {
            return ActionResult.PASS
        }
        if (!publicInventory.canPlayerUse(player)) {
            return ActionResult.PASS
        }
        (player as ServerPlayerEntityDuck).`publicenderchest$openInventory`("Public Inventory", publicInventory)
        return ActionResult.SUCCESS
    }

    override fun onItemInteractedListener(player: PlayerEntity, world: World, hand: Hand): TypedActionResult<ItemStack> {
        val heldStack: ItemStack = player.getStackInHand(hand)
        if (world.isClient() || player !is ServerPlayerEntity) {
            return TypedActionResult.pass(heldStack)
        }
        if (!heldStack.isEnderChest()) {
            return TypedActionResult.pass(heldStack)
        }
        val isUsingPublicInventory: Boolean = player.getAttached(PublicEnderChest.USING_PUBLIC_INVENTORY) ?: true
        if (!isUsingPublicInventory) {
            return TypedActionResult.pass(heldStack)
        }
        if (player.isSpectator) {
            return TypedActionResult.pass(heldStack)
        }
        if (!publicInventory.canPlayerUse(player)) {
            return TypedActionResult.pass(heldStack)
        }
        (player as ServerPlayerEntityDuck).`publicenderchest$openInventory`("Public Inventory", publicInventory)
        return TypedActionResult.success(heldStack)
    }

    companion object {

        private const val PUBLIC_INVENTORY_FILE_NAME: String = "public-inventory"
        val CODEC: Codec<PublicInventoryManager> = RecordCodecBuilder.create { instance ->
            instance.group(
                PublicInventory.CODEC.fieldOf("public_inventory").forGetter { manager -> manager.publicInventory }
            ).apply(instance) { publicInventory ->
                PublicInventoryManager(publicInventory)
            }
        }

    }

}