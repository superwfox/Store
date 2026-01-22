package sudark2.Sudark.store.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.PlayerStoreData;
import sudark2.Sudark.store.File.FileManager;
import sudark2.Sudark.store.File.TransactionManager;
import sudark2.Sudark.store.Menu.PlayerStoreMenu;
import sudark2.Sudark.store.Menu.SellManager;

import java.util.List;

import static sudark2.Sudark.store.Store.getInstance;

public class PlayerStoreListener implements Listener {

    @EventHandler
    public void onPlayerStoreClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (title.equals(PlayerStoreMenu.TITLE_PLAYER_STORE)) {
            e.setCancelled(true);
            if (clicked.getType() == Material.SUNFLOWER) {
                PlayerStoreMenu.openSellInput(p);
            } else if (clicked.getType() == Material.PLAYER_HEAD) {
                int slot = e.getSlot();
                List<PlayerStoreData.PlayerItem> items = PlayerStoreData.getPlayerItems();
                if (slot > 0 && slot <= items.size()) {
                    PlayerStoreMenu.openPlayerItemView(p, items.get(slot - 1));
                }
            }
        }
        else if (title.equals(PlayerStoreMenu.TITLE_ITEM_DETAIL)) {
            e.setCancelled(true);
            if (e.getSlot() == 53 && clicked.getType() == Material.GOLD_INGOT && e.getClick() == ClickType.LEFT) {
                PlayerStoreData.PlayerItem item = PlayerStoreMenu.getViewingItem(p.getUniqueId());
                if (item != null) {
                    if (p.getLevel() >= item.price) {
                        p.setLevel(p.getLevel() - item.price);
                        for (ItemStack stack : item.items) {
                            p.getInventory().addItem(stack.clone());
                        }
                        
                        TransactionManager.recordTransaction(item.playerName, p.getName(), item.price, item.items);
                        TransactionManager.addPayback(item.playerId, item.playerName, item.price);
                        
                        PlayerStoreData.removePlayerItem(item);
                        FileManager.saveData();
                        p.closeInventory();
                        p.sendMessage("§7购买成功");
                    } else {
                        p.sendMessage("§7经验等级不足");
                    }
                }
            }
        }
        else if (title.equals(PlayerStoreMenu.TITLE_SELL_INPUT)) {
        }
    }

    @EventHandler
    public void onPlayerStoreClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        String title = e.getView().getTitle();

        if (title.equals(PlayerStoreMenu.TITLE_SELL_INPUT)) {
            Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                SellManager.handleSellClose(p, e.getInventory());
            }, 1L);
        }
        else if (title.equals(PlayerStoreMenu.TITLE_ITEM_DETAIL)) {
            PlayerStoreData.PlayerItem item = PlayerStoreMenu.getViewingItem(p.getUniqueId());
            if (item != null) {
                PlayerStoreMenu.removeViewingItem(p.getUniqueId());
                PlayerStoreMenu.openPlayerStore(p);
            }
        }
    }
}
