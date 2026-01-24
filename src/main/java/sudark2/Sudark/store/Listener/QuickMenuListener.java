package sudark2.Sudark.store.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Menu.OfficialStoreMenu;
import sudark2.Sudark.store.Menu.PlayerStoreMenu;
import sudark2.Sudark.store.Menu.QuickMenu;

import static sudark2.Sudark.store.Util.MethodUtil.isLocValid;

public class QuickMenuListener implements Listener {

    @EventHandler
    public void onQuickMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p))
            return;
        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();

        if (!title.equals(QuickMenu.TITLE))
            return;

        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        e.setCancelled(true);
        int slot = e.getSlot();
        if (slot == 11 && clicked.getType() == Material.GOLD_INGOT) {
            PlayerStoreMenu.openPlayerStore(p);
        } else if (slot == 15 && clicked.getType() == Material.NETHERITE_INGOT) {
            OfficialStoreMenu.openOfficialStore(p);
        }
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e) {
        Player pl = e.getPlayer();
        if (isLocValid(pl) && pl.isSneaking()) {
            e.setCancelled(true);
            QuickMenu.open(pl);
        }
    }
}
