package sudark2.Sudark.store.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import sudark2.Sudark.store.File.TransactionManager;
import sudark2.Sudark.store.Menu.QuickMenu;

import static sudark2.Sudark.store.Util.MethodUtil.isLocValid;

public class PlayerGlobalListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        TransactionManager.processPayback(p);
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e) {
        Player pl = e.getPlayer();
        if (pl.isSneaking()) {
            if (isLocValid(pl)) {
                e.setCancelled(true);
                QuickMenu.open(pl);
            }
        }
    }
}
