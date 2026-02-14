package sudark2.Sudark.store.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import sudark2.Sudark.store.File.TransactionManager;
import sudark2.Sudark.store.NPC.NPCManager;
import sudark2.Sudark.store.Store;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        TransactionManager.processPayback(p);

        Bukkit.getScheduler().runTaskLater(Store.getInstance(), () -> {
            NPCManager.showToPlayer(p);
        }, 20L);
    }
}
