package xd.arkosammy.publicenderchest.util.ducks;

import net.minecraft.inventory.Inventory;
import xd.arkosammy.publicenderchest.logging.InventoryInteractionLog;
import xd.arkosammy.publicenderchest.logging.QueryContext;

import java.util.List;

public interface ServerPlayerEntityDuck {

    void publicenderchest$openInventory(String name, Inventory inventory);

    void publicenderchest$setPageIndex(int pageIndex);

    List<List<InventoryInteractionLog>> publicenderchest$getCachedLogs();

    void publicenderchest$showLogs(QueryContext queryContext);

    void publicenderchest$showPage();

    void publicenderchest$setHasMod(boolean hasMod);

    boolean publicenderchest$hasMod();

}
