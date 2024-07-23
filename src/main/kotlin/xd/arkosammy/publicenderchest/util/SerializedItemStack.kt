package xd.arkosammy.publicenderchest.util

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack

class SerializedItemStack(val slotIndex: Int, inputItemStack: ItemStack) {

    val itemStack: ItemStack

    init {
        if (inputItemStack.isEmpty) {
            throw IllegalArgumentException("Cannot serialize empty item stack!")
        }
        itemStack = inputItemStack
    }

    companion object {

        val CODEC: Codec<SerializedItemStack> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("slot_index").forGetter(SerializedItemStack::slotIndex),
                ItemStack.CODEC.fieldOf("item_stack").forGetter(SerializedItemStack::itemStack)
            ).apply(instance) { slotIndex, itemStack ->
                SerializedItemStack(slotIndex, itemStack)
            }
        }

    }

}
