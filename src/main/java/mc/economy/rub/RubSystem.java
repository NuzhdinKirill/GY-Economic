package mc.economy.rub;

import lombok.Getter;
import mc.GY;
import org.bukkit.command.PluginCommand;

public class RubSystem {
    private final GY plugin;
    @Getter
    private RubDB database;
    @Getter
    private RubCommand command;

    public RubSystem(GY plugin) {
        this.plugin = plugin;
    }

    public void init() {
        database = new RubDB(plugin);
        database.init();
        command = new RubCommand(plugin, this);
    }

    public void registerCommands() {
        PluginCommand rubCmd = plugin.getCommand("rub");
        PluginCommand payrubCmd = plugin.getCommand("payrub");

        if (rubCmd != null) {
            rubCmd.setExecutor(command);
            rubCmd.setTabCompleter(command);
        }
        if (payrubCmd != null) {
            payrubCmd.setExecutor(command);
            payrubCmd.setTabCompleter(command);
        }
    }

    public void shutdown() {
        if (database != null) {
            database.close();
        }
    }

}