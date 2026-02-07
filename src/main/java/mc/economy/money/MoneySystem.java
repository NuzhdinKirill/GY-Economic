package mc.economy.money;

import mc.GY;
import org.bukkit.command.PluginCommand;

public class MoneySystem {
    private final GY plugin;
    private MoneyDB database;
    private MoneyCommand command;

    public MoneySystem(GY plugin) {
        this.plugin = plugin;
    }

    public void init() {
        database = new MoneyDB(plugin);
        database.init();
        command = new MoneyCommand(plugin, this);
    }

    public void registerCommands() {
        PluginCommand moneyCmd = plugin.getCommand("money");
        PluginCommand payCmd = plugin.getCommand("pay");

        if (moneyCmd != null) {
            moneyCmd.setExecutor(command);
            moneyCmd.setTabCompleter(command);
        }
        if (payCmd != null) {
            payCmd.setExecutor(command);
            payCmd.setTabCompleter(command);
        }
    }

    public void shutdown() {
        if (database != null) {
            database.close();
        }
    }

    public MoneyDB getDatabase() {
        return database;
    }

    public MoneyCommand getCommand() {
        return command;
    }
}