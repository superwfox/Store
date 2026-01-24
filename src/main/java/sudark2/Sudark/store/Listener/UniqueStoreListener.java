package sudark2.Sudark.store.Listener;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.UniqueStoreData;
import sudark2.Sudark.store.File.UniqueStoreManager;
import sudark2.Sudark.store.Menu.UniqueStoreMenu;

import java.util.List;

public class UniqueStoreListener implements Listener {

    @EventHandler
    public void onUniqueStoreClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p))
            return;
        String title = e.getView().getTitle();

        if (!title.startsWith(UniqueStoreMenu.TITLE_PREFIX))
            return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == org.bukkit.Material.AIR)
            return;

        e.setCancelled(true);

        String npcKey = UniqueStoreMenu.getViewingStore(p.getName());
        if (npcKey == null)
            return;

        List<UniqueStoreData.UniqueItem> items = UniqueStoreData.getStoreItems(npcKey);
        int slot = e.getSlot();

        if (slot >= 0 && slot < items.size()) {
            UniqueStoreData.UniqueItem item = items.get(slot);

            if (e.getClick() == ClickType.RIGHT && p.isOp()) {
                UniqueStoreData.removeItem(npcKey, item);
                UniqueStoreManager.saveStore(npcKey);
                p.sendMessage("§7已删除商品");
                p.playSound(p, Sound.BLOCK_BARREL_CLOSE, 1, 1);
                String npcId = title.substring(UniqueStoreMenu.TITLE_PREFIX.length());
                UniqueStoreMenu.openUniqueStore(p, npcKey, npcId);
            } else if (e.getClick() == ClickType.LEFT) {
                if (p.getLevel() >= item.price) {
                    p.setLevel(p.getLevel() - item.price);
                    p.getInventory().addItem(item.item.clone());
                    p.sendMessage("§7购买成功");
                    p.playSound(p, Sound.ENTITY_VILLAGER_YES, 1, 1);
                } else {
                    p.sendMessage("§7经验等级不足");
                    p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                }
            }
        }
    }

    @EventHandler
    public void onUniqueStoreClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        Player p = (Player) e.getPlayer();
        String title = e.getView().getTitle();

        if (title.startsWith(UniqueStoreMenu.TITLE_PREFIX)) {
            UniqueStoreMenu.removeViewingStore(p.getName());
        }
    }
}
