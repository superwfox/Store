package sudark2.Sudark.store.Menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sudark2.Sudark.store.Data.PlayerStoreData;

import java.util.*;

public class PlayerStoreMenu {

    public static final String TITLE_PLAYER_STORE = "玩家商店 | §lPlayer";
    public static final String TITLE_ITEM_DETAIL = "商品 | 详情 §l§7Details";
    public static final String TITLE_SELL_INPUT = "请放入你要售卖的§6所有物品";

    private static final Map<UUID, PlayerStoreData.PlayerItem> viewingItem = new HashMap<>();

    public static void openPlayerStore(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PLAYER_STORE);

        ItemStack sunflower = new ItemStack(Material.LEGACY_OBSIDIAN);
        ItemMeta sm = sunflower.getItemMeta();
        sm.setDisplayName("§e出售物品");
        sm.setLore(List.of("§7点击后 放入要出售的物品们", "§7退出自动保存 3分钟无操作取消"));
        sunflower.setItemMeta(sm);
        inv.setItem(0, sunflower);

        List<PlayerStoreData.PlayerItem> items = PlayerStoreData.getPlayerItems();
        int slot = 1;
        for (PlayerStoreData.PlayerItem item : items) {
            if (slot >= 54)
                break;
            if (item.items.isEmpty())
                continue;

            ItemStack display = item.items.get(0).clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add("§f卖家: §e" + item.playerName);
            lore.add("§f售价: §e" + item.price);
            lore.add("§f时间: §7" + item.time);
            lore.add("§f备注: " + item.info);
            boolean canDelete = p.isOp() || item.playerId.equals(p.getUniqueId());
            if (canDelete) {
                lore.add("");
                lore.add("§7右键可下架并返还物品");
            }
            meta.setLore(lore);
            display.setItemMeta(meta);

            if (item.playerId.equals(p.getUniqueId())) {
                display.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
            }

            inv.setItem(slot++, display);
        }

        p.openInventory(inv);
    }

    public static void openSellInput(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_SELL_INPUT);
        p.openInventory(inv);
    }

    public static void openPlayerItemView(Player p, PlayerStoreData.PlayerItem item) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_ITEM_DETAIL);

        int slot = 0;
        for (ItemStack stack : item.items) {
            if (slot >= 53)
                break;
            inv.setItem(slot++, stack.clone());
        }

        ItemStack confirm = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = confirm.getItemMeta();
        meta.setDisplayName("§e确认购买");
        List<String> lore = new ArrayList<>();
        lore.add("§f需要: §e" + item.price);
        boolean canDelete = p.isOp() || item.playerId.equals(p.getUniqueId());
        if (canDelete) {
            lore.add("");
            lore.add("§7右键物品可撤回单个物品");
        }
        meta.setLore(lore);
        confirm.setItemMeta(meta);
        inv.setItem(53, confirm);

        viewingItem.put(p.getUniqueId(), item);
        p.openInventory(inv);
    }

    public static PlayerStoreData.PlayerItem getViewingItem(UUID uuid) {
        return viewingItem.get(uuid);
    }

    public static void removeViewingItem(UUID uuid) {
        viewingItem.remove(uuid);
    }
}
