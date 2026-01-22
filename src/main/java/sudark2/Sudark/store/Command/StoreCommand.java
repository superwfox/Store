package sudark2.Sudark.store.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.OfficialStoreData;
import sudark2.Sudark.store.File.FileManager;
import sudark2.Sudark.store.File.OfficialStoreManager;
import sudark2.Sudark.store.Menu.OfficialStoreMenu;
import sudark2.Sudark.store.Menu.PlayerStoreMenu;
import sudark2.Sudark.store.NPC.InitNPC;
import sudark2.Sudark.store.Util.MethodUtil;

import java.util.Optional;

import static sudark2.Sudark.store.File.FileManager.loadNPCs;

public class StoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§7仅玩家可用");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            PlayerStoreMenu.openPlayerStore(p);
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "create":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store create <商店ID>");
                    return true;
                }
                InitNPC.init(p, args[1]);
                break;

            case "player":
                PlayerStoreMenu.openPlayerStore(p);
                break;

            case "official":
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store official <商店ID>");
                    return true;
                }
                String npcId = args[1];
                String npcKey = OfficialStoreData.getNPCKey(npcId);
                if (npcKey == null) {
                    p.sendMessage("§7商店不存在");
                    return true;
                }
                OfficialStoreMenu.openOfficialStore(p, npcKey, npcId);
                break;

            case "add":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store add <价格> [备注]");
                    return true;
                }

                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand.getType() == org.bukkit.Material.AIR) {
                    p.sendMessage("§7请手持物品");
                    return true;
                }

                try {
                    int price = Integer.parseInt(args[1]);
                    String info = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "§7无";

                    Optional<String> nearestKey = MethodUtil.findNearestNPC(p, 50);
                    if (!nearestKey.isPresent()) {
                        p.sendMessage("§7附近没有商店NPC");
                        return true;
                    }

                    String targetKey = nearestKey.get();
                    OfficialStoreData.OfficialItem item = new OfficialStoreData.OfficialItem(hand.clone(), price, info);
                    OfficialStoreData.addItem(targetKey, item);
                    OfficialStoreManager.saveStore(targetKey);
                    p.sendMessage("§7已添加商品到最近的商店");
                } catch (NumberFormatException e) {
                    p.sendMessage("§7价格必须为整数");
                }
                break;
                
            case "reload":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                OfficialStoreData.clearAll();
                FileManager.loadData();

                org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), "npc remove all");
                loadNPCs();
                p.sendMessage("§e已重载插件并重置所有NPC");
                break;
                
            case "destroy":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store destroy <商店ID>");
                    return true;
                }
                String destroyId = args[1];
                String destroyKey = OfficialStoreData.getNPCKey(destroyId);
                if (destroyKey == null) {
                    p.sendMessage("§7商店不存在");
                    return true;
                }
                
                org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), "npc remove " + destroyId);
                OfficialStoreData.removeNPC(destroyId);
                OfficialStoreData.removeStore(destroyKey);
                OfficialStoreManager.deleteStore(destroyKey);
                OfficialStoreManager.saveAll();
                p.sendMessage("§e已销毁商店: " + destroyId);
                break;

            default:
                PlayerStoreMenu.openPlayerStore(p);
                break;
        }

        return true;
    }
}
