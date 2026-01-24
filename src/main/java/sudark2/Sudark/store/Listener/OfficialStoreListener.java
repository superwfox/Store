package sudark2.Sudark.store.Listener;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.OfficialStoreData;
import sudark2.Sudark.store.File.OfficialStoreManager;
import sudark2.Sudark.store.Menu.OfficialStoreMenu;
import sudark2.Sudark.store.Util.MethodUtil;

import java.util.List;

public class OfficialStoreListener implements Listener {

    @EventHandler
    public void onOfficialStoreClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p))
            return;
        String title = e.getView().getTitle();

        if (!title.equals(OfficialStoreMenu.TITLE))
            return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == org.bukkit.Material.AIR)
            return;

        e.setCancelled(true);

        List<OfficialStoreData.OfficialItem> items = OfficialStoreData.getOfficialItems();
        int slot = e.getSlot();

        if (slot >= 0 && slot < items.size()) {
            OfficialStoreData.OfficialItem item = items.get(slot);

            if (e.getClick() == ClickType.RIGHT && p.isOp()) {
                OfficialStoreData.removeItem(slot);
                OfficialStoreManager.cleanupItemFiles();
                OfficialStoreManager.saveAll();
                p.sendMessage("§7已删除商品");
                p.playSound(p, Sound.BLOCK_BARREL_CLOSE, 1, 1);
                OfficialStoreMenu.openOfficialStore(p);
            } else if (e.getClick() == ClickType.LEFT) {
                MethodUtil.purchase(p, item.price, item.item);
            }
        }
    }
}
