package sudark2.Sudark.store.Menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RecycleStoreMenu {

    public static final String TITLE = "回收商店 | §lRecycle";

    public static void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        p.openInventory(inv);
    }
}
