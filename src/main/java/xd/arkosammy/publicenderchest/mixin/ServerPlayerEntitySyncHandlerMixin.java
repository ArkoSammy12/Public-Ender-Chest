package xd.arkosammy.publicenderchest.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.publicenderchest.inventory.CustomGenericContainerScreenHandler;
import xd.arkosammy.publicenderchest.inventory.PublicInventory;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$1")
public abstract class ServerPlayerEntitySyncHandlerMixin {

    /*
    @WrapOperation(method = "updateSlot", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket"))
    private ScreenHandlerSlotUpdateS2CPacket modifyItemStack(int syncId, int revision, int slot, ItemStack stack, Operation<ScreenHandlerSlotUpdateS2CPacket> original) {
        if (!(((ServerPlayerEntity) (Object) this).currentScreenHandler instanceof CustomGenericContainerScreenHandler screenHandler)) {
            return original.call(syncId, revision, slot, stack);
        }
        Inventory inventory = screenHandler.getInventory();
        if (!(inventory instanceof PublicInventory publicInventory)) {
            return original.call(syncId, revision, slot, stack);
        }
        List<Text> customText = publicInventory.getCustomTextForItemInSlot(slot);
        if (customText.isEmpty()) {
            return original.call(syncId, revision, slot, stack);
        }
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            return original.call(syncId, revision, slot, stack);
        }
        List<Text> originalLines = loreComponent.lines();
        List<Text> newLines = new ArrayList<>(originalLines);
        newLines.addAll(customText);
        LoreComponent newLore = new LoreComponent(ImmutableList.copyOf(newLines), loreComponent.styledLines());
        ItemStack newStack = stack.copy();
        newStack.set(DataComponentTypes.LORE, newLore);
        return original.call(syncId, revision, slot, newStack);
    }



    @WrapOperation(method = "updateSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void modifyItemStack2(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original, ScreenHandler handler, int slot, ItemStack stack) {
        if (!(handler instanceof CustomGenericContainerScreenHandler screenHandler)) {
            original.call(instance, packet);
            return;
        }
        Inventory inventory = screenHandler.getInventory();
        if (!(inventory instanceof PublicInventory publicInventory)) {
            original.call(instance, packet);
            return;
        }
        List<Text> customText = publicInventory.getCustomTextForItemInSlot(slot);
        if (customText.isEmpty()) {
            original.call(instance, packet);
            return;
        }
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            original.call(instance, packet);
            return;
        }
        List<Text> originalLines = loreComponent.lines();
        List<Text> newLines = new ArrayList<>(originalLines);
        newLines.addAll(customText);
        LoreComponent newLore = new LoreComponent(ImmutableList.copyOf(newLines), loreComponent.styledLines());
        ItemStack newStack = stack.copy();
        newStack.set(DataComponentTypes.LORE, newLore);
        ScreenHandlerSlotUpdateS2CPacket newPacket = new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), slot, newStack);
        original.call(instance, newPacket);
    }

     */

}
