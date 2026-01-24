package sudark2.Sudark.store.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import sudark2.Sudark.store.Data.PlayerStoreData;

import java.io.*;
import java.util.*;

public class PlayerStoreManager {

    private static File dataFolder;
    private static File itemsFolder;
    private static File configFile;

    public static void init(File pluginDataFolder) {
        dataFolder = pluginDataFolder;
        itemsFolder = new File(dataFolder, "items");
        if (!itemsFolder.exists())
            itemsFolder.mkdirs();
        configFile = new File(dataFolder, "data.yml");
    }

    public static void loadAll() {
        if (!configFile.exists())
            return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        PlayerStoreData.clearAll();

        List<Map<?, ?>> playerList = config.getMapList("playerItems");
        for (Map<?, ?> map : playerList) {
            try {
                String name = (String) map.get("name");
                UUID id = UUID.fromString((String) map.get("uuid"));
                int price = (int) map.get("price");
                String time = (String) map.get("time");
                String info = (String) map.get("info");
                String itemFile = (String) map.get("itemFile");

                List<ItemStack> items = loadItems(itemFile);
                PlayerStoreData.PlayerItem item = new PlayerStoreData.PlayerItem(name, id, items, price, time, info);
                PlayerStoreData.addPlayerItem(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveAll() {
        YamlConfiguration config = new YamlConfiguration();

        List<Map<String, Object>> playerList = new ArrayList<>();
        for (PlayerStoreData.PlayerItem item : PlayerStoreData.getPlayerItems()) {
            Map<String, Object> map = new HashMap<>();
            String fileName = "player_" + item.playerId.toString() + "_" + System.currentTimeMillis() + ".dat";

            map.put("name", item.playerName);
            map.put("uuid", item.playerId.toString());
            map.put("price", item.price);
            map.put("time", item.time);
            map.put("info", item.info);
            map.put("itemFile", fileName);

            saveItems(fileName, item.items);
            playerList.add(map);
        }
        config.set("playerItems", playerList);

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveItems(String fileName, List<ItemStack> items) {
        File file = new File(itemsFolder, fileName);
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(file))) {
            out.writeInt(items.size());
            for (ItemStack item : items) {
                out.writeObject(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<ItemStack> loadItems(String fileName) {
        File file = new File(itemsFolder, fileName);
        if (!file.exists())
            return new ArrayList<>();

        List<ItemStack> items = new ArrayList<>();
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(file))) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                items.add((ItemStack) in.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return items;
    }
}
