package sudark2.Sudark.store.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import sudark2.Sudark.store.Data.PlayerStoreData;

import java.io.*;
import java.util.*;

public class FileManager {

    private static JavaPlugin plugin;
    private static File dataFolder;
    private static File itemsFolder;
    private static File configFile;

    public static void init(JavaPlugin p) {
        plugin = p;
        dataFolder = p.getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdirs();

        itemsFolder = new File(dataFolder, "items");
        if (!itemsFolder.exists())
            itemsFolder.mkdirs();

        configFile = new File(dataFolder, "data.yml");

        UniqueStoreManager.init(dataFolder);
        OfficialStoreManager.init(dataFolder);
        TransactionManager.init(dataFolder);
    }

    public static void loadData() {
        loadPlayerItems();
        UniqueStoreManager.loadAll();
        OfficialStoreManager.loadAll();
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
        savePlayerItems();
        UniqueStoreManager.saveAll();
    }

    private static void loadPlayerItems() {
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

    private static void savePlayerItems() {
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
