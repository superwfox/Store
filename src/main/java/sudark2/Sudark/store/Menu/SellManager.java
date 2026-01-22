package sudark2.Sudark.store.Menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.PlayerStoreData;
import sudark2.Sudark.store.File.FileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static sudark2.Sudark.store.Store.getInstance;

public class SellManager {

    private static final Map<UUID, List<ItemStack>> pendingSell = new HashMap<>();
    private static final Map<UUID, Integer> pendingPrice = new HashMap<>();
    private static final Set<UUID> waitingForPrice = new HashSet<>();
    private static final Set<UUID> waitingForInfo = new HashSet<>();
    private static final Map<UUID, Long> inputTimeout = new HashMap<>();
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MM-dd-HH:mm");

    public static void handleSellClose(Player p, Inventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() != org.bukkit.Material.AIR) {
                items.add(stack.clone());
            }
        }

        if (items.isEmpty()) return;

        UUID uuid = p.getUniqueId();
        pendingSell.put(uuid, items);
        long timeout = System.currentTimeMillis() + 180000;
        inputTimeout.put(uuid, timeout);
        waitingForPrice.add(uuid);

        p.sendMessage("§e请在聊天栏输入价格（纯数字）");
        
        Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
            if (waitingForPrice.contains(uuid) || waitingForInfo.contains(uuid)) {
                waitingForPrice.remove(uuid);
                waitingForInfo.remove(uuid);
                pendingSell.remove(uuid);
                pendingPrice.remove(uuid);
                inputTimeout.remove(uuid);
                p.sendMessage("§7输入超时，上架已取消");
            }
        }, 3600L);
    }

    public static void handlePriceInput(Player p, String input) {
        UUID uuid = p.getUniqueId();
        if (!pendingSell.containsKey(uuid)) return;

        try {
            int price = Integer.parseInt(input);
            pendingPrice.put(uuid, price);
            waitingForPrice.remove(uuid);
            waitingForInfo.add(uuid);
            p.sendMessage("§e请在聊天栏输入备注信息（输入”无“表示无备注）");
        } catch (NumberFormatException e) {
            p.sendMessage("§7价格必须为整数，请重新输入");
        }
    }

    public static void handleInfoInput(Player p, String input) {
        UUID uuid = p.getUniqueId();
        List<ItemStack> items = pendingSell.remove(uuid);
        Integer price = pendingPrice.remove(uuid);
        inputTimeout.remove(uuid);
        waitingForInfo.remove(uuid);

        if (items == null || price == null) {
            p.sendMessage("§7数据丢失");
            return;
        }

        String info = input.equals("无") ? "§7无" : input;
        String time = LocalDateTime.now().format(timeFormat);

        PlayerStoreData.PlayerItem item = new PlayerStoreData.PlayerItem(
                p.getName(), p.getUniqueId(), items, price, time, info
        );
        PlayerStoreData.addPlayerItem(item);
        FileManager.saveData();

        p.sendMessage("§e商品已上架");
    }

    public static boolean isWaitingForPrice(UUID uuid) {
        return waitingForPrice.contains(uuid);
    }

    public static boolean isWaitingForInfo(UUID uuid) {
        return waitingForInfo.contains(uuid);
    }
}
