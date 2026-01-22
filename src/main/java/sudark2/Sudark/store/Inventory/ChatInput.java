package sudark2.Sudark.store.Inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import sudark2.Sudark.store.Menu.SellManager;

import java.util.Set;
import java.util.UUID;

public class ChatInput implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String msg = e.getMessage();

        if (SellManager.isWaitingForPrice(uuid)) {
            e.setCancelled(true);
            SellManager.handlePriceInput(p, msg);
        } else if (SellManager.isWaitingForInfo(uuid)) {
            e.setCancelled(true);
            SellManager.handleInfoInput(p, msg);
        }
    }
}
