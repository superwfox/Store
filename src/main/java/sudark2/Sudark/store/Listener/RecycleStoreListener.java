package sudark2.Sudark.store.Listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.RecycleStoreData;
import sudark2.Sudark.store.Menu.RecycleStoreMenu;
import sudark2.Sudark.store.Util.MethodUtil;

import java.util.Map;

public class RecycleStoreListener implements Listener {

    @EventHandler
    public void onRecycleClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p))
            return;
        if (!RecycleStoreMenu.TITLE.equals(e.getView().getTitle()))
            return;

        Map<String, RecycleStoreData.RecycleItem> recycleMap = RecycleStoreData.getAll();
        long totalExp = 0;
        boolean returnedAnyItem = false;
        boolean hasInput = false;

        for (ItemStack item : e.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR)
                continue;
            hasInput = true;

            RecycleStoreData.RecycleItem recycleItem = getRecycleItem(item, recycleMap);

            if (recycleItem == null || recycleItem.expLevel <= 0) {
                MethodUtil.giveItem(p, item);
                returnedAnyItem = true;
            } else {
                totalExp += (long) recycleItem.expLevel * item.getAmount();
            }
        }

        if (totalExp > 0) {
            int gained = (int) Math.min(Integer.MAX_VALUE, totalExp);
            int newLevel = (int) Math.min(Integer.MAX_VALUE, (long) p.getLevel() + gained);
            p.setLevel(newLevel);
            p.sendMessage("§e回收成功 §f+§b" + gained + " §f经验等级");
            if (returnedAnyItem)
                p.sendMessage("§7不可回收物品已返还");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
        } else {
            if (!hasInput) {
                p.sendMessage("§7未放入任何物品");
            } else if (returnedAnyItem) {
                p.sendMessage("§7未检测到可回收物品，物品已返还");
            } else {
                p.sendMessage("§7未检测到有效回收配置");
            }
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }
    }

    private RecycleStoreData.RecycleItem getRecycleItem(ItemStack item, Map<String, RecycleStoreData.RecycleItem> recycleMap) {
        String key = RecycleStoreData.getItemKey(item);
        if (key == null)
            return null;
        return recycleMap.get(key);
    }
}
