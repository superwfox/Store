package sudark2.Sudark.store.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import sudark2.Sudark.store.Data.UniqueStoreData;
import sudark2.Sudark.store.Menu.UniqueStoreMenu;
import sudark2.Sudark.store.Util.MethodUtil;

public class EntityClickEvent implements Listener {

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND)
            return;
        Entity en = e.getRightClicked();
        if (en instanceof Player npc && !Bukkit.getOnlinePlayers().contains(npc)) {
            String npcKey = MethodUtil.getLocCode(npc.getLocation());

            String npcId = findNPCId(npcKey);
            if (npcId != null) {
                UniqueStoreMenu.openUniqueStore(e.getPlayer(), npcKey, npcId);
            }
        }
    }

    private String findNPCId(String npcKey) {
        for (var entry : UniqueStoreData.getNPCMapping().entrySet()) {
            if (entry.getValue().equals(npcKey)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
