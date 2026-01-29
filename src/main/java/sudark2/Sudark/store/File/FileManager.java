package sudark2.Sudark.store.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
        File npcListFile = new File(dataFolder, "npcList.yml");
        if (!npcListFile.exists())
            return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(npcListFile);

        for (String npcId : config.getKeys(false)) {
            String npcKey = config.getString(npcId);
            if (npcKey == null)
                continue;

            String[] parts = npcKey.split("_");
            if (parts.length != 4)
                continue;

            String worldName = parts[0];
            String x = parts[1];
            String y = parts[2];
            String z = parts[3];

            String command = String.format("npc create %s --at %s,%s,%s,%s",
                    npcId, x, y, z, worldName);

            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                org.bukkit.Bukkit.dispatchCommand(
                        org.bukkit.Bukkit.getConsoleSender(),
                        command);
            });
        }
    }

    public static void saveData() {
        PlayerStoreManager.saveAll();
        UniqueStoreManager.saveAll();
    }
}
