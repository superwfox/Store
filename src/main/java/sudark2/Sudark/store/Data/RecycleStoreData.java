package sudark2.Sudark.store.Data;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class RecycleStoreData {

    private static final Map<String, RecycleItem> recycleMap = new HashMap<>();

    public static class RecycleItem {
        public ItemStack item;
        public int expLevel;

        public RecycleItem(ItemStack item, int expLevel) {
            this.item = item;
            this.expLevel = expLevel;
        }

        public int getRequiredAmount() {
            return item.getAmount();
        }
    }

    public static String getItemKey(ItemStack item) {
        try {
            ItemStack normalized = item.clone();
            normalized.setAmount(1);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(normalized);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static void addItem(ItemStack item, int expLevel) {
        String key = getItemKey(item);
        if (key != null) {
            recycleMap.put(key, new RecycleItem(item.clone(), expLevel));
        }
    }

    public static RecycleItem findMatch(ItemStack item) {
        String key = getItemKey(item);
        return key != null ? recycleMap.get(key) : null;
    }

    public static Map<String, RecycleItem> getAll() {
        return recycleMap;
    }

    public static void clearAll() {
        recycleMap.clear();
    }

    public static void removeByKey(String key) {
        recycleMap.remove(key);
    }
}
