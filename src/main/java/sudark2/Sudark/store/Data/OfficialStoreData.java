package sudark2.Sudark.store.Data;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OfficialStoreData {

    private static final List<OfficialItem> officialItems = new ArrayList<>();

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

    public static List<OfficialItem> getOfficialItems() {
        return officialItems;
    }

    public static void addItem(OfficialItem item) {
        if (officialItems.size() < 54) {
            officialItems.add(item);
        }
    }

    public static void removeItem(int index) {
        if (index >= 0 && index < officialItems.size()) {
            officialItems.remove(index);
        }
    }

    public static void clearAll() {
        officialItems.clear();
    }

    public static void setItems(List<OfficialItem> items) {
        officialItems.clear();
        officialItems.addAll(items);
    }
}
