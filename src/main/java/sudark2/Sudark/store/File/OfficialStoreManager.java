package sudark2.Sudark.store.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import sudark2.Sudark.store.Data.OfficialStoreData;

import java.io.*;
import java.util.*;

public class OfficialStoreManager {

    private static File dataFolder;
    private static File itemsFolder;
    private static File configFile;

    public static void init(File pluginDataFolder) {
        dataFolder = pluginDataFolder;
        itemsFolder = new File(dataFolder, "OfficialItems");
        if (!itemsFolder.exists())
            itemsFolder.mkdirs();
        configFile = new File(dataFolder, "OfficialData.yml");
    }

    public static void loadAll() {
        if (!configFile.exists())
            return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        OfficialStoreData.clearAll();

        List<Map<?, ?>> itemList = config.getMapList("items");
        for (int i = 0; i < itemList.size(); i++) {
            Map<?, ?> map = itemList.get(i);
            try {
                int price = (int) map.get("price");
                String info = (String) map.get("info");
                ItemStack item = loadItem(i);
                if (item != null) {
                    OfficialStoreData.addItem(new OfficialStoreData.OfficialItem(item, price, info));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveAll() {
        YamlConfiguration config = new YamlConfiguration();

        List<Map<String, Object>> itemList = new ArrayList<>();
        List<OfficialStoreData.OfficialItem> items = OfficialStoreData.getOfficialItems();

        for (int i = 0; i < items.size(); i++) {
            OfficialStoreData.OfficialItem item = items.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("price", item.price);
            map.put("info", item.info);
            itemList.add(map);
            saveItem(i, item.item);
        }

        config.set("items", itemList);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveItem(int index, ItemStack item) {
        File file = new File(itemsFolder, index + ".dat");
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ItemStack loadItem(int index) {
        File file = new File(itemsFolder, index + ".dat");
        if (!file.exists())
            return null;

        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(file))) {
            return (ItemStack) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void cleanupItemFiles() {
        File[] files = itemsFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }
}
