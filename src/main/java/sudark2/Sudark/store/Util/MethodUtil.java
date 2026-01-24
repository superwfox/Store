package sudark2.Sudark.store.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.Objects;
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
        int y = Objects.requireNonNull(loc.getWorld()).getHighestBlockYAt(x, z) + 1;
        return loc.getWorld().getName() + "_" +
                x + "_" +
                y + "_" +
                z;
    }

    public static boolean isLocValid(Player pl) {
        if (pl.isOp()) return true;
        if (!pl.getWorld().getName().equals("world")) {
            pl.sendMessage("[§e商店§f] §7只能在主世界使用");
            return false;
        }
        return true;
    }
}
