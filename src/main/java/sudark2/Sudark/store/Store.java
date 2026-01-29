package sudark2.Sudark.store;

import org.bukkit.plugin.java.JavaPlugin;
import sudark2.Sudark.store.Command.StoreCommand;
import sudark2.Sudark.store.Command.StoreTabCompleter;
import sudark2.Sudark.store.File.FileManager;
import sudark2.Sudark.store.Inventory.ChatInput;
import sudark2.Sudark.store.Listener.*;

public final class Store extends JavaPlugin {

    private static Store instance;

    @Override
    public void onEnable() {
        instance = this;
        FileManager.init(this);
        FileManager.loadData();

        getCommand("store").setExecutor(new StoreCommand());
        getCommand("store").setTabCompleter(new StoreTabCompleter());
        getServer().getPluginManager().registerEvents(new PlayerStoreListener(), this);
        getServer().getPluginManager().registerEvents(new OfficialStoreListener(), this);
        getServer().getPluginManager().registerEvents(new UniqueStoreListener(), this);
        getServer().getPluginManager().registerEvents(new QuickMenuListener(), this);
        getServer().getPluginManager().registerEvents(new RecycleStoreListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInput(), this);
        getServer().getPluginManager().registerEvents(new EntityClickEvent(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    @Override
    public void onDisable() {
        FileManager.saveData();
    }

    public static Store getInstance() {
        return instance;
    }
}
