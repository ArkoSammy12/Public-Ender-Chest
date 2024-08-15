package xd.arkosammy.publicenderchest.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.publicenderchest.inventory.CustomGenericContainerScreenHandler;
import xd.arkosammy.publicenderchest.inventory.PublicInventory;
import xd.arkosammy.publicenderchest.inventory.PublicInventoryKt;
import xd.arkosammy.publicenderchest.util.CustomMutableText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @ModifyExpressionValue(method = "checkSlotUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean forceSlotUpdateForPublicInventory(boolean original, int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        if (stack.isEmpty()) {
            return original;
        }
        if (!(((ScreenHandler) (Object) this) instanceof CustomGenericContainerScreenHandler screenHandler)) {
            return original;
        }
        Inventory inventory = screenHandler.getInventory();
        if (!(inventory instanceof PublicInventory)) {
            return original;
        }
        // Expression value is inverted by a not operator
        return false;
    }

    @WrapOperation(method = "checkSlotUpdates", at =  @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerSyncHandler;updateSlot(Lnet/minecraft/screen/ScreenHandler;ILnet/minecraft/item/ItemStack;)V"))
    private void modifySentItemStack(ScreenHandlerSyncHandler instance, ScreenHandler screenHandler, int slot, ItemStack itemStack, Operation<Void> original) {
        if (!(screenHandler instanceof CustomGenericContainerScreenHandler customScreenHandler)) {
            original.call(instance, screenHandler, slot, itemStack);
            return;
        }
        Inventory inventory = customScreenHandler.getInventory();
        if (!(inventory instanceof PublicInventory publicInventory)) {
            original.call(instance, screenHandler, slot, itemStack);
            return;
        }
        List<CustomMutableText> customInfoLines = PublicInventoryKt.getCustomInfoLines(publicInventory.getStack(slot));
        if (customInfoLines.isEmpty()) {
            original.call(instance, screenHandler, slot, itemStack);
            return;
        }
        LoreComponent loreComponent = itemStack.get(DataComponentTypes.LORE);
        List<Text> newLines = new ArrayList<>();
        if (loreComponent != null) {
            newLines.addAll(loreComponent.lines());
        }
        newLines.addAll(customInfoLines);
        LoreComponent newLoreComponent;
        if (loreComponent != null) {
            newLoreComponent = new LoreComponent(ImmutableList.copyOf(newLines), ImmutableList.copyOf(loreComponent.styledLines()));
        } else {
            newLoreComponent = new LoreComponent(ImmutableList.copyOf(newLines));
        }
        ItemStack sentItemStack = itemStack.copy();
        sentItemStack.set(DataComponentTypes.LORE, newLoreComponent);
        original.call(instance, screenHandler, slot, sentItemStack);
    }

}
