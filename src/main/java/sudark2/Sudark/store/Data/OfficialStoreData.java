package sudark2.Sudark.store.Data;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OfficialStoreData {

    private static final ConcurrentHashMap<String, List<OfficialItem>> stores = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> npcMapping = new ConcurrentHashMap<>();

    public static class OfficialItem {
        public ItemStack item;
        public int price;
        public String info;

        public OfficialItem(ItemStack item, int price, String info) {
            this.item = item;
            this.price = price;
            this.info = info;
        }
    }

    public static List<OfficialItem> getStoreItems(String npcKey) {
        return stores.getOrDefault(npcKey, new ArrayList<>());
    }

    public static void addItem(String npcKey, OfficialItem item) {
        stores.computeIfAbsent(npcKey, k -> new ArrayList<>()).add(item);
    }

    public static void removeItem(String npcKey, OfficialItem item) {
        List<OfficialItem> items = stores.get(npcKey);
        if (items != null) {
            items.remove(item);
        }
    }

    public static void setStoreItems(String npcKey, List<OfficialItem> items) {
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

    public static ConcurrentHashMap<String, List<OfficialItem>> getAllStores() {
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
