package sudark2.Sudark.store.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import sudark2.Sudark.store.File.SellManager;
import sudark2.Sudark.store.Util.MethodUtil;

import java.util.List;

import static sudark2.Sudark.store.Store.getInstance;

public class PlayerStoreListener implements Listener {

    @EventHandler
    public void onPlayerStoreClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p))
            return;
        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        if (title.equals(PlayerStoreMenu.TITLE_PLAYER_STORE)) {
            handlePlayerStoreClick(e, p, clicked);
        } else if (title.equals(PlayerStoreMenu.TITLE_ITEM_DETAIL)) {
            handleItemDetailClick(e, p, clicked);
        }
    }

    private void handlePlayerStoreClick(InventoryClickEvent e, Player p, ItemStack clicked) {
        e.setCancelled(true);
        int slot = e.getSlot();
        if (clicked.getType() == Material.END_PORTAL_FRAME && slot == 0) {
            PlayerStoreMenu.openSellInput(p);
            return;
        }
        if (slot > 0 && slot < 54) {
            List<PlayerStoreData.PlayerItem> items = PlayerStoreData.getPlayerItems();
            if (slot <= items.size()) {
                PlayerStoreData.PlayerItem item = items.get(slot - 1);
                if (e.getClick() == ClickType.RIGHT) {
                    handleUnlistItem(p, item);
                } else {
                    PlayerStoreMenu.openPlayerItemView(p, item);
                }
            }
        }
    }

    private void handleUnlistItem(Player p, PlayerStoreData.PlayerItem item) {
        boolean canDelete = p.isOp() || item.playerId.equals(p.getUniqueId());
        if (canDelete) {
            MethodUtil.giveItems(p, item.items);
            PlayerStoreData.removePlayerItem(item);
            FileManager.saveData();
            p.sendMessage("§7已下架商品 物品已返还");
            p.playSound(p.getLocation(), Sound.BLOCK_BARREL_CLOSE, 1, 1);
            PlayerStoreMenu.openPlayerStore(p);
        } else {
            p.sendMessage("§7无权下架此商品");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }

    private void handleItemDetailClick(InventoryClickEvent e, Player p, ItemStack clicked) {
        e.setCancelled(true);
        PlayerStoreData.PlayerItem item = PlayerStoreMenu.getViewingItem(p.getUniqueId());
        if (item == null)
            return;

        if (e.getClick() == ClickType.RIGHT && e.getSlot() < 53) {
            handleWithdrawSingleItem(p, item, e.getSlot());
            return;
        }

        if (e.getSlot() == 53 && clicked.getType() == Material.GOLD_INGOT && e.getClick() == ClickType.LEFT) {
            handlePurchase(p, item);
        }
    }

    private void handleWithdrawSingleItem(Player p, PlayerStoreData.PlayerItem item, int slot) {
        boolean canDelete = p.isOp() || item.playerId.equals(p.getUniqueId());
        if (!canDelete) {
            p.sendMessage("§7无权撤回此物品");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }
        if (slot >= 0 && slot < item.items.size()) {
            ItemStack removed = item.items.remove(slot);
            MethodUtil.giveItem(p, removed.clone());
            if (item.items.isEmpty()) {
                PlayerStoreData.removePlayerItem(item);
                PlayerStoreMenu.removeViewingItem(p.getUniqueId());
                p.closeInventory();
                p.sendMessage("§7商品已清空，自动下架");
            } else {
                p.sendMessage("§7已返还一件物品");
                PlayerStoreMenu.openPlayerItemView(p, item);
            }
            FileManager.saveData();
            p.playSound(p.getLocation(), Sound.BLOCK_BARREL_CLOSE, 1, 1);
        }
    }

    private void handlePurchase(Player p, PlayerStoreData.PlayerItem item) {
        if (MethodUtil.purchase(p, item.price, item.items)) {
            TransactionManager.recordTransaction(item.playerName, p.getName(), item.price, item.items);
            TransactionManager.addPayback(item.playerId, item.playerName, item.price);
            PlayerStoreData.removePlayerItem(item);
            FileManager.saveData();
            p.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerStoreClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p))
            return;
        String title = e.getView().getTitle();

        if (title.equals(PlayerStoreMenu.TITLE_SELL_INPUT)) {
            SellManager.handleSellClose(p, e.getInventory());
        } else if (title.equals(PlayerStoreMenu.TITLE_ITEM_DETAIL)) {
            PlayerStoreData.PlayerItem item = PlayerStoreMenu.getViewingItem(p.getUniqueId());
            if (item != null) {
                PlayerStoreMenu.removeViewingItem(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                    PlayerStoreMenu.openPlayerStore(p);
                }, 1L);
            }
        }
    }
}
