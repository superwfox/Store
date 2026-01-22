package sudark2.Sudark.store.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sudark2.Sudark.store.Data.OfficialStoreData;
import sudark2.Sudark.store.File.OfficialStoreManager;
import sudark2.Sudark.store.Util.MethodUtil;

public class InitNPC {

    private InitNPC() {}

    public static void init(Player pl, String npcId) {
        Bukkit.dispatchCommand(pl, "npc create " + npcId);
        
        String npcKey = MethodUtil.getLocCode(pl.getLocation());
        
        OfficialStoreData.registerNPC(npcId, npcKey);
        OfficialStoreManager.saveAll();
        
        pl.sendMessage("§7已创建商店NPC: " + npcId);
    }
}
