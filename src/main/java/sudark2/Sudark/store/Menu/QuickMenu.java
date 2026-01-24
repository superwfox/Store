package sudark2.Sudark.store.Menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class QuickMenu {

    public static final String TITLE = "商店选择 | §lSelect";

    public static void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack gold = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldMeta = gold.getItemMeta();
        goldMeta.setDisplayName("§e玩家商店");
        List<String> goldLore = new ArrayList<>();
        goldLore.add("§7点击打开玩家商店");
        goldMeta.setLore(goldLore);
        gold.setItemMeta(goldMeta);

        ItemStack netherite = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta netheriteMeta = netherite.getItemMeta();
        netheriteMeta.setDisplayName("§b官方商店");
        List<String> netheriteLore = new ArrayList<>();
        netheriteLore.add("§7点击打开官方商店");
        netheriteMeta.setLore(netheriteLore);
        netherite.setItemMeta(netheriteMeta);

        inv.setItem(11, gold);
        inv.setItem(15, netherite);

        p.openInventory(inv);
    }
}
