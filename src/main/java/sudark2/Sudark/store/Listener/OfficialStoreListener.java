package sudark2.Sudark.store.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.OfficialStoreData;
import sudark2.Sudark.store.File.OfficialStoreManager;
import sudark2.Sudark.store.Menu.OfficialStoreMenu;

import java.util.List;

public class OfficialStoreListener implements Listener {

    @EventHandler
    public void onOfficialStoreClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        if (!title.startsWith(OfficialStoreMenu.TITLE_PREFIX)) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == org.bukkit.Material.AIR) return;

        e.setCancelled(true);

        String npcKey = OfficialStoreMenu.getViewingStore(p.getName());
        if (npcKey == null) return;

        List<OfficialStoreData.OfficialItem> items = OfficialStoreData.getStoreItems(npcKey);
        int slot = e.getSlot();

        if (slot >= 0 && slot < items.size()) {
            OfficialStoreData.OfficialItem item = items.get(slot);

            if (e.getClick() == ClickType.RIGHT && p.isOp()) {
                OfficialStoreData.removeItem(npcKey, item);
                OfficialStoreManager.saveStore(npcKey);
                p.sendMessage("§7已删除商品");
                String npcId = title.substring(OfficialStoreMenu.TITLE_PREFIX.length());
                OfficialStoreMenu.openOfficialStore(p, npcKey, npcId);
            } else if (e.getClick() == ClickType.LEFT) {
                if (p.getLevel() >= item.price) {
                    p.setLevel(p.getLevel() - item.price);
                    p.getInventory().addItem(item.item.clone());
                    p.sendMessage("§7购买成功");
                } else {
                    p.sendMessage("§7经验等级不足");
                }
            }
        }
    }

    @EventHandler
    public void onOfficialStoreClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        String title = e.getView().getTitle();

        if (title.startsWith(OfficialStoreMenu.TITLE_PREFIX)) {
            OfficialStoreMenu.removeViewingStore(p.getName());
        }
    }
}
