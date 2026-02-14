package sudark2.Sudark.store.NPC;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sudark2.Sudark.store.File.UniqueStoreManager;
import sudark2.Sudark.store.Store;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SkinFetcher {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static void fetchAndApply(Player sender, String playerName, String npcKey) {
        Bukkit.getScheduler().runTaskAsynchronously(Store.getInstance(), () -> {
            String[] result = fetchSkin(playerName);
            Bukkit.getScheduler().runTask(Store.getInstance(), () -> {
                if (result == null) {
                    sender.sendMessage("§7找不到正版玩家: §e" + playerName);
                    return;
                }
                NPCManager.applySkin(npcKey, result[0], result[1]);
                UniqueStoreManager.saveAll();
                sender.sendMessage("§7已将最近NPC的皮肤设置为: §e" + playerName);
            });
        });
    }

    private static String[] fetchSkin(String playerName) {
        try {
            String uuid = getUUID(playerName);
            if (uuid == null) return null;
            HttpURLConnection conn = (HttpURLConnection) new URL(PROFILE_URL + uuid + "?unsigned=false").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) return null;
            JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            JsonObject prop = json.getAsJsonArray("properties").get(0).getAsJsonObject();
            return new String[]{prop.get("value").getAsString(), prop.get("signature").getAsString()};
        } catch (Exception e) {
            return null;
        }
    }

    private static String getUUID(String playerName) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(UUID_URL + playerName).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) return null;
            JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            return json.get("id").getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}
