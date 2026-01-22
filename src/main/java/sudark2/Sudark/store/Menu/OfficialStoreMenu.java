package sudark2.Sudark.store.Menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sudark2.Sudark.store.Data.OfficialStoreData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OfficialStoreMenu {

    public static final String TITLE_PREFIX = "官方商店 | §l";

    private static final ConcurrentHashMap<String, String> viewingStore = new ConcurrentHashMap<>();

    public static void openOfficialStore(Player p, String npcKey, String npcId) {
        String title = TITLE_PREFIX + npcId;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<OfficialStoreData.OfficialItem> items = OfficialStoreData.getStoreItems(npcKey);
        int slot = 0;
        for (OfficialStoreData.OfficialItem item : items) {
            if (slot >= 54) break;

            ItemStack display = item.item.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add("§f售价: §e" + item.price + " §f级");
            lore.add("§f备注: " + item.info);

            if (p.isOp()) {
                lore.add("");
                lore.add("§7您可以用 §f右键§7 来删除该商品");
            }

            meta.setLore(lore);
            display.setItemMeta(meta);

            inv.setItem(slot++, display);
        }

        viewingStore.put(p.getName(), npcKey);
        p.openInventory(inv);
    }

    public static String getViewingStore(String name) {
        return viewingStore.get(name);
    }

    public static void removeViewingStore(String name) {
        viewingStore.remove(name);
    }
}
