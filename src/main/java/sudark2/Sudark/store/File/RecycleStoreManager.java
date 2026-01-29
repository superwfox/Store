package sudark2.Sudark.store.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import sudark2.Sudark.store.Data.RecycleStoreData;

import java.io.*;
import java.util.*;

public class RecycleStoreManager {

    private static File dataFolder;
    private static File itemsFolder;
    private static File configFile;

    public static void init(File pluginDataFolder) {
        dataFolder = pluginDataFolder;
        itemsFolder = new File(dataFolder, "RecycleItems");
        if (!itemsFolder.exists())
            itemsFolder.mkdirs();
        configFile = new File(dataFolder, "RecycleData.yml");
    }

    public static void loadAll() {
        if (!configFile.exists())
            return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        RecycleStoreData.clearAll();

        for (String key : config.getKeys(false)) {
            try {
                int expLevel = config.getInt(key + ".exp");
                ItemStack item = loadItem(key);
                if (item != null) {
                    RecycleStoreData.addItem(item, expLevel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveAll() {
        YamlConfiguration config = new YamlConfiguration();

        int index = 0;
        for (Map.Entry<String, RecycleStoreData.RecycleItem> entry : RecycleStoreData.getAll().entrySet()) {
            RecycleStoreData.RecycleItem recycleItem = entry.getValue();
            String idx = String.valueOf(index);
            config.set(idx + ".exp", recycleItem.expLevel);
            saveItem(idx, recycleItem.item);
            index++;
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveItem(String key, ItemStack item) {
        File file = new File(itemsFolder, key + ".dat");
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ItemStack loadItem(String key) {
        File file = new File(itemsFolder, key + ".dat");
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
