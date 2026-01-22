package sudark2.Sudark.store.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import sudark2.Sudark.store.Data.OfficialStoreData;

import java.io.*;
import java.util.*;

public class OfficialStoreManager {

    private static File storesFolder;
    private static File npcListFile;

    public static void init(File dataFolder) {
        storesFolder = new File(dataFolder, "officialStores");
        if (!storesFolder.exists()) storesFolder.mkdirs();
        
        npcListFile = new File(dataFolder, "npcList.yml");
    }

    public static void loadAll() {
        loadNPCMapping();
        
        for (String npcKey : OfficialStoreData.getNPCMapping().values()) {
            loadStore(npcKey);
        }
    }

    public static void saveAll() {
        saveNPCMapping();
        
        for (Map.Entry<String, List<OfficialStoreData.OfficialItem>> entry : OfficialStoreData.getAllStores().entrySet()) {
            saveStore(entry.getKey(), entry.getValue());
        }
    }

    private static void loadNPCMapping() {
        if (!npcListFile.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(npcListFile);
        for (String npcId : config.getKeys(false)) {
            String npcKey = config.getString(npcId);
            OfficialStoreData.registerNPC(npcId, npcKey);
        }
    }

    private static void saveNPCMapping() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, String> entry : OfficialStoreData.getNPCMapping().entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        
        try {
            config.save(npcListFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadStore(String npcKey) {
        File storeFile = new File(storesFolder, npcKey + ".dat");
        if (!storeFile.exists()) return;
        
        List<OfficialStoreData.OfficialItem> items = new ArrayList<>();
        
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(storeFile))) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                ItemStack item = (ItemStack) in.readObject();
                int price = in.readInt();
                String info = in.readUTF();
                items.add(new OfficialStoreData.OfficialItem(item, price, info));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        OfficialStoreData.setStoreItems(npcKey, items);
    }

    private static void saveStore(String npcKey, List<OfficialStoreData.OfficialItem> items) {
        File storeFile = new File(storesFolder, npcKey + ".dat");
        
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(storeFile))) {
            out.writeInt(items.size());
            for (OfficialStoreData.OfficialItem item : items) {
                out.writeObject(item.item);
                out.writeInt(item.price);
                out.writeUTF(item.info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStore(String npcKey) {
        List<OfficialStoreData.OfficialItem> items = OfficialStoreData.getStoreItems(npcKey);
        saveStore(npcKey, items);
    }

    public static void deleteStore(String npcKey) {
        File storeFile = new File(storesFolder, npcKey + ".dat");
        if (storeFile.exists()) {
            storeFile.delete();
        }
    }
}
