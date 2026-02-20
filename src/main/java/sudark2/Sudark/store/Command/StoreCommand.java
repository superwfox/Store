package sudark2.Sudark.store.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.store.Data.OfficialStoreData;
import sudark2.Sudark.store.Data.RecycleStoreData;
import sudark2.Sudark.store.Data.UniqueStoreData;
import sudark2.Sudark.store.File.FileManager;
import sudark2.Sudark.store.File.OfficialStoreManager;
import sudark2.Sudark.store.File.RecycleStoreManager;
import sudark2.Sudark.store.File.UniqueStoreManager;
import sudark2.Sudark.store.Menu.OfficialStoreMenu;
import sudark2.Sudark.store.Menu.RecycleStoreMenu;
import sudark2.Sudark.store.Menu.UniqueStoreMenu;
import sudark2.Sudark.store.Menu.PlayerStoreMenu;
import sudark2.Sudark.store.NPC.InitNPC;
import sudark2.Sudark.store.NPC.NPCManager;
import sudark2.Sudark.store.NPC.SkinFetcher;
import sudark2.Sudark.store.Util.MethodUtil;

import java.util.Optional;

public class StoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§7仅玩家可用");
            return true;
        }

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
                if (MethodUtil.isLocValid(p))
                    PlayerStoreMenu.openPlayerStore(p);
                break;

            case "official":
                if (MethodUtil.isLocValid(p))
                    OfficialStoreMenu.openOfficialStore(p);
                break;

            case "recycle":
                if (MethodUtil.isLocValid(p)) {
                    RecycleStoreMenu.open(p);
                    p.sendMessage("§7放入物品后关闭界面自动回收");
                }
                break;

            case "cycle":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store cycle <经验等级>");
                    return true;
                }
                ItemStack cycleHand = p.getInventory().getItemInMainHand();
                if (cycleHand.getType() == org.bukkit.Material.AIR) {
                    p.sendMessage("§7请手持物品");
                    return true;
                }
                try {
                    int expLevel = Integer.parseInt(args[1]);
                    if (expLevel <= 0) {
                        p.sendMessage("§7经验等级必须大于0");
                        return true;
                    }
                    if (RecycleStoreData.getItemKey(cycleHand) == null) {
                        p.sendMessage("§7该物品无法加入回收配置");
                        return true;
                    }
                    RecycleStoreData.addItem(cycleHand.clone(), expLevel);
                    RecycleStoreManager.saveAll();
                    p.sendMessage("§7已添加可回收物品: §e" + cycleHand.getType() + " §6： " + expLevel + "§f经验等级");
                } catch (NumberFormatException e) {
                    p.sendMessage("§7经验等级必须为整数");
                }
                break;

            case "check":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store check <商店ID>");
                    return true;
                }
                String npcId = args[1];
                String npcKey = UniqueStoreData.getNPCKey(npcId);
                if (npcKey == null) {
                    p.sendMessage("§7商店不存在");
                    return true;
                }
                UniqueStoreMenu.openUniqueStore(p, npcKey, npcId);
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
                    String info = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length))
                            : "§7无";

                    Optional<String> nearestKey = MethodUtil.findNearestNPC(p, 50);
                    if (!nearestKey.isPresent()) {
                        p.sendMessage("§7附近没有商店NPC");
                        return true;
                    }

                    String targetKey = nearestKey.get();
                    UniqueStoreData.UniqueItem item = new UniqueStoreData.UniqueItem(hand.clone(), price, info);
                    UniqueStoreData.addItem(targetKey, item);
                    UniqueStoreManager.saveStore(targetKey);
                    p.sendMessage("§7已添加商品到最近的商店");
                } catch (NumberFormatException e) {
                    p.sendMessage("§7价格必须为整数");
                }
                break;

            case "update":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store update <价格> [备注]");
                    return true;
                }

                ItemStack updateHand = p.getInventory().getItemInMainHand();
                if (updateHand.getType() == org.bukkit.Material.AIR) {
                    p.sendMessage("§7请手持物品");
                    return true;
                }

                try {
                    int updatePrice = Integer.parseInt(args[1]);
                    String updateInfo = args.length > 2
                            ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length))
                            : "§7无";

                    if (OfficialStoreData.getOfficialItems().size() >= 54) {
                        p.sendMessage("§7官方商店已满");
                        return true;
                    }

                    OfficialStoreData.OfficialItem officialItem = new OfficialStoreData.OfficialItem(updateHand.clone(),
                            updatePrice, updateInfo);
                    OfficialStoreData.addItem(officialItem);
                    OfficialStoreManager.saveAll();
                    p.sendMessage("§7已添加商品到官方商店");
                } catch (NumberFormatException e) {
                    p.sendMessage("§7价格必须为整数");
                }
                break;

            case "reload":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                NPCManager.removeAll();
                UniqueStoreData.clearAll();
                FileManager.loadData();
                FileManager.loadNPCs();
                p.sendMessage("§e已重载插件并重置所有NPC");
                break;

            case "setskin":
                if (!p.isOp()) {
                    p.sendMessage("§7无权限");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("§7用法: /store setskin <正版玩家ID>");
                    return true;
                }
                Optional<String> skinTarget = MethodUtil.findNearestNPC(p, 50);
                if (skinTarget.isEmpty()) {
                    p.sendMessage("§7附近没有商店NPC");
                    return true;
                }
                String skinKey = skinTarget.get();
                p.sendMessage("§7正在获取皮肤...");
                SkinFetcher.fetchAndApply(p, args[1], skinKey);
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
                String destroyKey = UniqueStoreData.getNPCKey(destroyId);
                if (destroyKey == null) {
                    p.sendMessage("§7商店不存在");
                    return true;
                }

                NPCManager.remove(destroyKey);
                UniqueStoreData.removeNPC(destroyId);
                UniqueStoreData.removeStore(destroyKey);
                UniqueStoreData.removeSkin(destroyKey);
                UniqueStoreManager.deleteStore(destroyKey);
                UniqueStoreManager.saveAll();
                p.sendMessage("§e已销毁商店: " + destroyId);
                break;

            default:
                PlayerStoreMenu.openPlayerStore(p);
                break;
        }

        return true;
    }
}
