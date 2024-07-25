package xd.arkosammy.publicenderchest

import com.mojang.serialization.Codec
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.EnderChestBlock
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.WorldSavePath
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xd.arkosammy.monkeyconfig.managers.ConfigManager
import xd.arkosammy.monkeyconfig.managers.TomlConfigManager
import xd.arkosammy.monkeyconfig.managers.getSettingValue
import xd.arkosammy.monkeyconfig.registrars.DefaultConfigRegistrar
import xd.arkosammy.monkeyconfig.settings.NumberSetting
import xd.arkosammy.publicenderchest.PublicEnderChest.MOD_ID
import xd.arkosammy.publicenderchest.config.ConfigSettings
import xd.arkosammy.publicenderchest.config.SettingGroups
import xd.arkosammy.publicenderchest.inventory.PublicInventoryManager
import xd.arkosammy.publicenderchest.logging.InventoryDatabaseManager
import xd.arkosammy.publicenderchest.util.Events
import java.nio.file.Path

object PublicEnderChest : ModInitializer {

	const val MOD_ID: String = "publicenderchest"
	const val PUBLIC_INVENTORY_FILE_NAME: String = "public-inventory"
	val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
	val CONFIG_MANAGER: ConfigManager = TomlConfigManager(MOD_ID, SettingGroups.settingGroups, ConfigSettings.settingBuilders)
	val INVENTORY_MANAGER: PublicInventoryManager = PublicInventoryManager()
	val USING_PUBLIC_INVENTORY: AttachmentType<Boolean> = run<AttachmentType<Boolean>> {
		val builder: AttachmentRegistry.Builder<Boolean> = AttachmentRegistryImpl.builder()
		builder.copyOnDeath().persistent(Codec.BOOL).initializer {true}.buildAndRegister(Identifier.of(MOD_ID, "using_public_inventory"))
	}
	lateinit var DATABASE_MANAGER: InventoryDatabaseManager
		private set

	override fun onInitialize() {
		DefaultConfigRegistrar.registerConfigManager(CONFIG_MANAGER)
		ServerLifecycleEvents.SERVER_STARTING.register(::onServerStarting)
		ServerLifecycleEvents.SERVER_STOPPING.register(::onServerStopping)
		Events.registerEvents()
	}

	private fun onServerStarting(server: MinecraftServer) {
		DATABASE_MANAGER = InventoryDatabaseManager(server)
		INVENTORY_MANAGER.onServerStarting(server)
	}

	private fun onServerStopping(server: MinecraftServer) {
		INVENTORY_MANAGER.onServerStopping(server)
		DATABASE_MANAGER.purge(server, CONFIG_MANAGER.getSettingValue<Int, NumberSetting<Int>>(ConfigSettings.PURGE_OLDER_THAN_X_DAYS.settingLocation))
	}

}

fun getModFolderPath(server: MinecraftServer) : Path = server.getSavePath(WorldSavePath.ROOT).resolve(MOD_ID)

fun BlockState.isEnderChest() : Boolean = this.isOf(Blocks.ENDER_CHEST)
fun ItemStack.isEnderChest() : Boolean {
	val item: Item = this.item
	if (item !is BlockItem) {
		return false
	}
	return item.block is EnderChestBlock
}