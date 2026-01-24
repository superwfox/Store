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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.PlayerStoreData;
import sudark2.Sudark.store.File.FileManager;
import sudark2.Sudark.store.File.TransactionManager;
import sudark2.Sudark.store.Menu.OfficialStoreMenu;
import sudark2.Sudark.store.Menu.PlayerStoreMenu;
import sudark2.Sudark.store.Menu.QuickMenu;
import sudark2.Sudark.store.Menu.SellManager;

import java.util.List;

import static sudark2.Sudark.store.Store.getInstance;
import static sudark2.Sudark.store.Util.MethodUtil.isLocValid;

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
            e.setCancelled(true);
            int slot = e.getSlot();
            if (clicked.getType() == Material.LEGACY_OBSIDIAN && slot == 0) {
                PlayerStoreMenu.openSellInput(p);
            } else if (slot > 0 && slot < 54) {
                List<PlayerStoreData.PlayerItem> items = PlayerStoreData.getPlayerItems();
                if (slot <= items.size()) {
                    PlayerStoreData.PlayerItem item = items.get(slot - 1);
                    if (e.getClick() == ClickType.RIGHT) {
                        boolean canDelete = p.isOp() || item.playerId.equals(p.getUniqueId());
                        if (canDelete) {
                            for (ItemStack stack : item.items) {
                                if (p.getInventory().firstEmpty() == -1) {
                                    p.getWorld().dropItem(p.getLocation(), stack.clone());
                                } else {
                                    p.getInventory().addItem(stack.clone());
                                }
                            }
                            PlayerStoreData.removePlayerItem(item);
                            FileManager.saveData();
                            p.sendMessage("§7已下架商品 物品已返还");
                            p.playSound(p.getLocation(), Sound.BLOCK_BARREL_CLOSE, 1, 1);
                            PlayerStoreMenu.openPlayerStore(p);
                        } else {
                            p.sendMessage("§7无权下架此商品");
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        }
                    } else {
                        PlayerStoreMenu.openPlayerItemView(p, item);
                    }
                }
            }
        } else if (title.equals(PlayerStoreMenu.TITLE_ITEM_DETAIL)) {
            e.setCancelled(true);
            PlayerStoreData.PlayerItem item = PlayerStoreMenu.getViewingItem(p.getUniqueId());
            if (item == null)
                return;

            if (e.getClick() == ClickType.RIGHT && e.getSlot() < 53) {
                boolean canDelete = p.isOp() || item.playerId.equals(p.getUniqueId());
                if (!canDelete) {
                    p.sendMessage("§7无权撤回此物品");
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                int clickedSlot = e.getSlot();
                if (clickedSlot >= 0 && clickedSlot < item.items.size()) {
                    ItemStack removed = item.items.remove(clickedSlot);
                    if (p.getInventory().firstEmpty() == -1) {
                        p.getWorld().dropItem(p.getLocation(), removed.clone());
                    } else {
                        p.getInventory().addItem(removed.clone());
                    }
                    if (item.items.isEmpty()) {
                        PlayerStoreData.removePlayerItem(item);
                        PlayerStoreMenu.removeViewingItem(p.getUniqueId());
                        p.closeInventory();
                        p.sendMessage("§7商品已清空，自动下架");
                        p.playSound(p.getLocation(), Sound.BLOCK_BARREL_CLOSE, 1, 1);
                    } else {
                        p.sendMessage("§7已返还一件物品");
                        p.playSound(p.getLocation(), Sound.BLOCK_BARREL_CLOSE, 1, 1);
                        PlayerStoreMenu.openPlayerItemView(p, item);
                    }
                    FileManager.saveData();
                    p.playSound(p.getLocation(), Sound.BLOCK_BARREL_CLOSE, 1, 1);
                }
                return;
            }

            if (e.getSlot() == 53 && clicked.getType() == Material.GOLD_INGOT && e.getClick() == ClickType.LEFT) {
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
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                } else {
                    p.sendMessage("§7经验等级不足");
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.5f);
                }
            }
        } else if (title.equals(PlayerStoreMenu.TITLE_SELL_INPUT)) {
        } else if (title.equals(QuickMenu.TITLE)) {
            e.setCancelled(true);
            int slot = e.getSlot();
            if (slot == 11 && clicked.getType() == Material.GOLD_INGOT) {
                PlayerStoreMenu.openPlayerStore(p);
            } else if (slot == 15 && clicked.getType() == Material.NETHERITE_INGOT) {
                OfficialStoreMenu.openOfficialStore(p);
            }
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
