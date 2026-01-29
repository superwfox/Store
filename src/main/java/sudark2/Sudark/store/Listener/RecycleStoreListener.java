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

import java.util.HashMap;
import java.util.Map;

public class RecycleStoreListener implements Listener {

    @EventHandler
    public void onRecycleClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p))
            return;
        if (!e.getView().getTitle().equals(RecycleStoreMenu.TITLE))
            return;

        Map<String, Integer> collected = new HashMap<>();
        int totalExp = 0;
        boolean hasShortage = false;

        for (ItemStack item : e.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR)
                continue;

            String key = RecycleStoreData.getItemKey(item);
            RecycleStoreData.RecycleItem recycleItem = key != null ? RecycleStoreData.getAll().get(key) : null;

            if (recycleItem == null) {
                MethodUtil.giveItem(p, item);
            } else {
                collected.merge(key, item.getAmount(), Integer::sum);
            }
        }

        for (var entry : collected.entrySet()) {
            RecycleStoreData.RecycleItem recycleItem = RecycleStoreData.getAll().get(entry.getKey());
            int required = recycleItem.getRequiredAmount();
            int times = entry.getValue() / required;
            int remainder = entry.getValue() % required;

            totalExp += recycleItem.expLevel * times;

            if (remainder > 0) {
                ItemStack returnItem = recycleItem.item.clone();
                returnItem.setAmount(remainder);
                MethodUtil.giveItem(p, returnItem);
                hasShortage = true;
            }
        }

        if (totalExp > 0) {
            p.setLevel(p.getLevel() + totalExp);
            p.sendMessage("§e回收成功 §f+§b" + totalExp + " §f经验等级");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
        } else {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        }

        if (hasShortage) {
            p.sendMessage("§7部分物品数量不足，已返还");
        }
    }
}
