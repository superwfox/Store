package sudark2.Sudark.store.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import sudark2.Sudark.store.Data.UniqueStoreData;
import sudark2.Sudark.store.Menu.UniqueStoreMenu;
import sudark2.Sudark.store.NPC.NPCManager;

public class EntityClickEvent implements Listener {

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        String npcKey = NPCManager.getNpcKeyByEntityId(e.getRightClicked().getEntityId());
        if (npcKey == null) return;

        for (var entry : UniqueStoreData.getNPCMapping().entrySet()) {
            if (entry.getValue().equals(npcKey)) {
                UniqueStoreMenu.openUniqueStore(e.getPlayer(), npcKey, entry.getKey());
                return;
            }
        }
    }
}
