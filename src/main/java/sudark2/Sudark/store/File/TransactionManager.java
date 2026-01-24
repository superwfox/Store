package sudark2.Sudark.store.File;

import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionManager {

    private static File paybackFile;
    private static File recordsFile;
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void init(File dataFolder) {
        paybackFile = new File(dataFolder, "payback.yml");
        recordsFile = new File(dataFolder, "Records.csv");

        if (!recordsFile.exists()) {
            try {
                recordsFile.createNewFile();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(recordsFile), StandardCharsets.UTF_8))) {
                    writer.write("发布者,购买者,金额,时间,物品内容\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void recordTransaction(String seller, String buyer, int price, List<ItemStack> items) {
        String time = LocalDateTime.now().format(timeFormat);
        StringBuilder itemDesc = new StringBuilder();

        for (ItemStack item : items) {
            if (itemDesc.length() > 0)
                itemDesc.append("; ");
            itemDesc.append(item.getType().name())
                    .append("x").append(item.getAmount());
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemDesc.append("(").append(item.getItemMeta().getDisplayName()).append(")");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(recordsFile, true), StandardCharsets.UTF_8))) {
            writer.write(String.format("%s,%s,%d,%s,%s\n",
                    seller, buyer, price, time, itemDesc.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addPayback(UUID sellerId, String sellerName, int amount) {
        Player seller = org.bukkit.Bukkit.getPlayer(sellerId);
        if (seller != null && seller.isOnline()) {
            seller.setLevel(seller.getLevel() + amount);
            seller.sendMessage("§e您的商品已售出，获得 " + amount + " 级经验");
            seller.playSound(seller.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(paybackFile);
        String key = sellerId.toString();
        int current = config.getInt(key + ".amount", 0);
        config.set(key + ".amount", current + amount);
        config.set(key + ".name", sellerName);
        try {
            config.save(paybackFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processPayback(Player player) {
        if (!paybackFile.exists())
            return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(paybackFile);
        String key = player.getUniqueId().toString();

        if (config.contains(key)) {
            int amount = config.getInt(key + ".amount", 0);
            if (amount > 0) {
                player.setLevel(player.getLevel() + amount);
                player.sendMessage("§e您的商品已售出，获得 " + amount + " 级经验");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                config.set(key, null);
                try {
                    config.save(paybackFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
