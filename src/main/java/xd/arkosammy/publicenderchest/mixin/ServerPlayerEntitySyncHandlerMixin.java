package xd.arkosammy.publicenderchest.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.publicenderchest.PublicEnderChestKt;
import xd.arkosammy.publicenderchest.inventory.CustomGenericContainerScreenHandler;
import xd.arkosammy.publicenderchest.inventory.PublicInventory;
import xd.arkosammy.publicenderchest.inventory.PublicInventoryKt;
import xd.arkosammy.publicenderchest.util.CustomMutableText;
import xd.arkosammy.publicenderchest.util.ducks.ServerPlayerEntityDuck;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$1")
public abstract class ServerPlayerEntitySyncHandlerMixin {

    @Shadow @Final private ServerPlayerEntity field_29182;

    @WrapOperation(method = "updateSlot", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket"))
    private ScreenHandlerSlotUpdateS2CPacket modifySentItemStack(int syncId, int revision, int slot, ItemStack itemStack, Operation<ScreenHandlerSlotUpdateS2CPacket> original, ScreenHandler screenHandler) {
        ServerPlayerEntity player = this.field_29182;
        if (!(screenHandler instanceof CustomGenericContainerScreenHandler customScreenHandler)) {
            return original.call(syncId, revision, slot, itemStack);
        }
        Inventory inventory = customScreenHandler.getInventory();
        if (!(inventory instanceof PublicInventory publicInventory)) {
            return original.call(syncId, revision, slot, itemStack);
        }
        List<CustomMutableText> customInfoLines = PublicInventoryKt.getCustomInfoLines(publicInventory.getStack(slot));
        if (customInfoLines.isEmpty()) {
            return original.call(syncId, revision, slot, itemStack);
        }
        if (((ServerPlayerEntityDuck) player).publicenderchest$hasMod()) {
            for (CustomMutableText line : customInfoLines) {
                line.getText().append(" " + PublicEnderChestKt.formatInfoTextMetadata(syncId, revision));
            }
        }
        LoreComponent loreComponent = itemStack.get(DataComponentTypes.LORE);
        List<Text> sentLines = new ArrayList<>();
        if (loreComponent != null) {
            sentLines.addAll(loreComponent.lines());
        }
        sentLines.addAll(customInfoLines);
        LoreComponent sentLoreComponent;
        if (loreComponent != null) {
            sentLoreComponent = new LoreComponent(ImmutableList.copyOf(sentLines), ImmutableList.copyOf(loreComponent.styledLines()));
        } else {
            sentLoreComponent = new LoreComponent(ImmutableList.copyOf(sentLines));
        }
        ItemStack sentItemStack = itemStack.copy();
        sentItemStack.set(DataComponentTypes.LORE, sentLoreComponent);
        return original.call(syncId, revision, slot, sentItemStack);
    }

}
