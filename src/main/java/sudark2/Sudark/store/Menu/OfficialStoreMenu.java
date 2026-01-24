package sudark2.Sudark.store.Menu;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sudark2.Sudark.store.Data.OfficialStoreData;

import java.util.*;

public class OfficialStoreMenu {

    public static final String TITLE = "官方商店 | §lOfficial";

    public static void openOfficialStore(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        List<OfficialStoreData.OfficialItem> items = OfficialStoreData.getOfficialItems();
        int slot = 0;
        for (OfficialStoreData.OfficialItem item : items) {
            if (slot >= 54)
                break;

            ItemStack display = item.item.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add("§f售价: §e" + item.price);
            lore.add("§f备注: " + item.info);

            if (p.isOp()) {
                lore.add("");
                lore.add("§7您可以用 §f右键§7 来删除该商品");
            }

            meta.setLore(lore);
            display.setItemMeta(meta);

            inv.setItem(slot++, display);
        }

        p.openInventory(inv);
        p.playSound(p, Sound.BLOCK_BARREL_OPEN, 1, 1);
    }
}
