package sudark2.Sudark.store.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import sudark2.Sudark.store.Data.UniqueStoreData;

import java.io.*;
import java.util.*;

public class UniqueStoreManager {

    private static File storesFolder;
    private static File npcListFile;

    public static void init(File dataFolder) {
        storesFolder = new File(dataFolder, "UniqueStores");
        if (!storesFolder.exists())
            storesFolder.mkdirs();

        npcListFile = new File(dataFolder, "npcList.yml");
    }

    public static void loadAll() {
        loadNPCMapping();

        for (String npcKey : UniqueStoreData.getNPCMapping().values()) {
            loadStore(npcKey);
        }
    }

    public static void saveAll() {
        saveNPCMapping();

        for (Map.Entry<String, List<UniqueStoreData.UniqueItem>> entry : UniqueStoreData.getAllStores().entrySet()) {
            saveStore(entry.getKey(), entry.getValue());
        }
    }

    private static void loadNPCMapping() {
        if (!npcListFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(npcListFile);

        if (config.isConfigurationSection("npcs")) {
            var section = config.getConfigurationSection("npcs");
            for (String npcId : section.getKeys(false)) {
                UniqueStoreData.registerNPC(npcId, section.getString(npcId));
            }
        }

        if (config.isConfigurationSection("skins")) {
            var section = config.getConfigurationSection("skins");
            for (String npcKey : section.getKeys(false)) {
                String value = section.getString(npcKey + ".value");
                String sig = section.getString(npcKey + ".signature");
                if (value != null && sig != null) {
                    UniqueStoreData.setSkin(npcKey, value, sig);
                }
            }
        }
    }

    private static void saveNPCMapping() {
        YamlConfiguration config = new YamlConfiguration();

        for (var entry : UniqueStoreData.getNPCMapping().entrySet()) {
            config.set("npcs." + entry.getKey(), entry.getValue());
        }

        for (var entry : UniqueStoreData.getSkinMapping().entrySet()) {
            config.set("skins." + entry.getKey() + ".value", entry.getValue()[0]);
            config.set("skins." + entry.getKey() + ".signature", entry.getValue()[1]);
        }

        try {
            config.save(npcListFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadStore(String npcKey) {
        File storeFile = new File(storesFolder, npcKey + ".dat");
        if (!storeFile.exists())
            return;

        List<UniqueStoreData.UniqueItem> items = new ArrayList<>();

        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(storeFile))) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                ItemStack item = (ItemStack) in.readObject();
                int price = in.readInt();
                String info = in.readUTF();
                items.add(new UniqueStoreData.UniqueItem(item, price, info));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        UniqueStoreData.setStoreItems(npcKey, items);
    }

    private static void saveStore(String npcKey, List<UniqueStoreData.UniqueItem> items) {
        File storeFile = new File(storesFolder, npcKey + ".dat");

        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(storeFile))) {
            out.writeInt(items.size());
            for (UniqueStoreData.UniqueItem item : items) {
                out.writeObject(item.item);
                out.writeInt(item.price);
                out.writeUTF(item.info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStore(String npcKey) {
        List<UniqueStoreData.UniqueItem> items = UniqueStoreData.getStoreItems(npcKey);
        saveStore(npcKey, items);
    }

    public static void deleteStore(String npcKey) {
        File storeFile = new File(storesFolder, npcKey + ".dat");
        if (storeFile.exists()) {
            storeFile.delete();
        }
    }
}
