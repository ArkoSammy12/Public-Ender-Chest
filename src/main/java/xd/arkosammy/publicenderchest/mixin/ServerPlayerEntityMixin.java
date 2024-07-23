package xd.arkosammy.publicenderchest.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xd.arkosammy.publicenderchest.util.ducks.ServerPlayerEntityDuck;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityDuck {


    @Shadow public abstract OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory);

    @Shadow public abstract boolean shouldDamagePlayer(PlayerEntity player);

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public void publicenderchest$openInventory(String name, Inventory inventory) {
        NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
        }, Text.literal(name));
        this.openHandledScreen(screenHandlerFactory);
    }
}
