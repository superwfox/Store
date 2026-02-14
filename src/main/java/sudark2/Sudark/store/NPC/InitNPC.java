package sudark2.Sudark.store.NPC;

import org.bukkit.entity.Player;
import sudark2.Sudark.store.Data.UniqueStoreData;
import sudark2.Sudark.store.File.UniqueStoreManager;
import sudark2.Sudark.store.Util.MethodUtil;

public class InitNPC {

    private InitNPC() {
    }

    public static void init(Player pl, String npcId) {
        NPCManager.spawn(npcId, pl.getLocation());

        String npcKey = MethodUtil.getLocCode(pl.getLocation());
        UniqueStoreData.registerNPC(npcId, npcKey);
        UniqueStoreManager.saveAll();

        pl.sendMessage("§7已创建商店NPC: §e" + npcId);
    }
}
