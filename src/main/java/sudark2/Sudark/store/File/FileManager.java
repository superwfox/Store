package sudark2.Sudark.store.File;

import org.bukkit.plugin.java.JavaPlugin;
import sudark2.Sudark.store.Data.UniqueStoreData;
import sudark2.Sudark.store.NPC.NPCManager;

import java.io.File;

public class FileManager {

    private static JavaPlugin plugin;
    private static File dataFolder;

    public static void init(JavaPlugin p) {
        plugin = p;
        dataFolder = p.getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdirs();

        PlayerStoreManager.init(dataFolder);
        UniqueStoreManager.init(dataFolder);
        OfficialStoreManager.init(dataFolder);
        RecycleStoreManager.init(dataFolder);
        TransactionManager.init(dataFolder);
    }

    public static void loadData() {
        PlayerStoreManager.loadAll();
        UniqueStoreManager.loadAll();
        OfficialStoreManager.loadAll();
        RecycleStoreManager.loadAll();
    }

    public static void loadNPCs() {
        NPCManager.respawnAll(UniqueStoreData.getNPCMapping());
    }

    public static void saveData() {
        PlayerStoreManager.saveAll();
        UniqueStoreManager.saveAll();
    }
}
