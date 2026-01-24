package sudark2.Sudark.store.Data;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UniqueStoreData {

    private static final ConcurrentHashMap<String, List<UniqueItem>> stores = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> npcMapping = new ConcurrentHashMap<>();

    public static class UniqueItem {
        public ItemStack item;
        public int price;
        public String info;

        public UniqueItem(ItemStack item, int price, String info) {
            this.item = item;
            this.price = price;
            this.info = info;
        }
    }

    public static List<UniqueItem> getStoreItems(String npcKey) {
        return stores.getOrDefault(npcKey, new ArrayList<>());
    }

    public static void addItem(String npcKey, UniqueItem item) {
        stores.computeIfAbsent(npcKey, k -> new ArrayList<>()).add(item);
    }

    public static void removeItem(String npcKey, UniqueItem item) {
        List<UniqueItem> items = stores.get(npcKey);
        if (items != null) {
            items.remove(item);
        }
    }

    public static void setStoreItems(String npcKey, List<UniqueItem> items) {
        stores.put(npcKey, items);
    }

    public static void registerNPC(String npcId, String npcKey) {
        npcMapping.put(npcId, npcKey);
    }

    public static String getNPCKey(String npcId) {
        return npcMapping.get(npcId);
    }

    public static ConcurrentHashMap<String, String> getNPCMapping() {
        return npcMapping;
    }

    public static ConcurrentHashMap<String, List<UniqueItem>> getAllStores() {
        return stores;
    }

    public static void removeNPC(String npcId) {
        npcMapping.remove(npcId);
    }

    public static void removeStore(String npcKey) {
        stores.remove(npcKey);
    }

    public static void clearAll() {
        stores.clear();
        npcMapping.clear();
    }
}
