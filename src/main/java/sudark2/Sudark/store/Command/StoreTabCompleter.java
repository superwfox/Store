package sudark2.Sudark.store.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import sudark2.Sudark.store.Data.UniqueStoreData;

import java.util.ArrayList;
import java.util.List;

public class StoreTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("player", "official", "recycle"));
            if (sender.isOp()) {
                options.addAll(List.of("add", "check", "create", "reload", "destroy", "update", "cycle"));
            }

            String input = args[0].toLowerCase();
            for (String option : options) {
                if (option.startsWith(input)) {
                    completions.add(option);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create") && sender.isOp()) {
                completions.add("<商店ID>");
            } else if (args[0].equalsIgnoreCase("add") && sender.isOp()) {
                completions.add("<价格>");
            } else if (args[0].equalsIgnoreCase("update") && sender.isOp()) {
                completions.add("<价格>");
            } else if (args[0].equalsIgnoreCase("check")) {
                completions.addAll(UniqueStoreData.getNPCMapping().keySet());
            } else if (args[0].equalsIgnoreCase("destroy") && sender.isOp()) {
                completions.addAll(UniqueStoreData.getNPCMapping().keySet());
            } else if (args[0].equalsIgnoreCase("cycle") && sender.isOp()) {
                completions.add("<经验等级>");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add") && sender.isOp()) {
                completions.add("[备注]");
            } else if (args[0].equalsIgnoreCase("update") && sender.isOp()) {
                completions.add("[备注]");
            }
        }

        return completions;
    }
}
