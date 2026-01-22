package sudark2.Sudark.store.Data;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStoreData {

    public static class PlayerItem {
        public String playerName;
        public UUID playerId;
        public List<ItemStack> items;
        public int price;
        public String time;
        public String info;

        public PlayerItem(String playerName, UUID playerId, List<ItemStack> items, int price, String time, String info) {
            this.playerName = playerName;
            this.playerId = playerId;
            this.items = items;
            this.price = price;
            this.time = time;
            this.info = info;
        }
    }

    private static final List<PlayerItem> playerItems = new ArrayList<>();

    public static List<PlayerItem> getPlayerItems() {
        return playerItems;
    }

    public static void addPlayerItem(PlayerItem item) {
        playerItems.add(item);
    }

    public static void removePlayerItem(PlayerItem item) {
        playerItems.remove(item);
    }

    public static void clearAll() {
        playerItems.clear();
    }
}
