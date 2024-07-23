package xd.arkosammy.publicenderchest

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import xd.arkosammy.publicenderchest.networking.OpenPublicInventoryPayload
import java.util.*

object PublicEnderChestClient : ClientModInitializer {

	override fun onInitializeClient() {

	}

	@JvmStatic
	fun sendOpenPublicInventoryPayload() {
		ClientPlayNetworking.send(OpenPublicInventoryPayload(UUID.randomUUID()))
	}

}