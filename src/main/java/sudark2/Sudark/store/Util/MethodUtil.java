package sudark2.Sudark.store.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.List;

import java.util.Optional;

public class MethodUtil {

    public static Optional<String> findNearestNPC(Player player, double maxDistance) {
        Optional<Entity> npc = player.getNearbyEntities(maxDistance, maxDistance, maxDistance).stream()
                .filter(entity -> entity instanceof Player pl && !Bukkit.getOnlinePlayers().contains(pl))
                .min(Comparator.comparingDouble(entity -> entity.getLocation().distance(player.getLocation())));

        if (npc.isPresent()) {
            Entity entity = npc.get();
            if (entity instanceof Player npcPlayer) {
                npcPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false));
            }
            return Optional.of(getNPCKey(entity));
        }

        return Optional.empty();
    }

    public static String getNPCKey(Entity npc) {
        return getLocCode(npc.getLocation());
    }

    public static String getLocCode(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int y = getBestBlockY(loc);
        return loc.getWorld().getName() + "_" +
                x + "_" +
                y + "_" +
                z;
    }

    public static int getBestBlockY(Location locOri) {
        Location loc = locOri.clone();
        int temY = loc.getBlockY();
        byte tried = -100;
        while (!loc.getBlock().getType().isSolid()) {
            if (tried < 0)
                loc.add(0, -1, 0);
            else
                loc.add(0, 1, 0);
            tried++;
            if (tried == 100) return temY;
        }
        return loc.getBlockY() + 1;
    }

    public static boolean isLocValid(Player pl) {
        if (pl.isOp())
            return true;
        if (!pl.getWorld().getName().equals("world")) {
            pl.sendMessage("[§e商店§f] §7只能在主世界使用");
            return false;
        }
        return true;
    }

    public static void giveItem(Player p, ItemStack item) {
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItem(p.getLocation(), item);
        } else {
            p.getInventory().addItem(item);
        }
    }

    public static void giveItems(Player p, List<ItemStack> items) {
        for (ItemStack item : items) {
            giveItem(p, item.clone());
        }
    }

    public static boolean purchase(Player p, int price, ItemStack item) {
        if (p.getLevel() >= price) {
            p.setLevel(p.getLevel() - price);
            p.getInventory().addItem(item.clone());
            p.sendMessage("§7购买成功");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            return true;
        } else {
            p.sendMessage("§7经验等级不足");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }
    }

    public static boolean purchase(Player p, int price, List<ItemStack> items) {
        if (p.getLevel() >= price) {
            p.setLevel(p.getLevel() - price);
            for (ItemStack item : items) {
                p.getInventory().addItem(item.clone());
            }
            p.sendMessage("§7购买成功");
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        } else {
            p.sendMessage("§7经验等级不足");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 0.5f);
            return false;
        }
    }
}
