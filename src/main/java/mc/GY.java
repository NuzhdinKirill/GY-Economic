package mc;

import lombok.Getter;
import mc.economy.money.MoneySystem;
import mc.economy.rub.RubSystem;
import mc.placeholder.Placeholders;
import org.bukkit.plugin.java.JavaPlugin;

public final class GY extends JavaPlugin {
    @Getter
    private static GY instance;
    @Getter
    private static MoneySystem moneySystem;
    @Getter
    private static RubSystem rubSystem;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        instance = this;

        moneySystem = new MoneySystem(this);
        moneySystem.init();

        rubSystem = new RubSystem(this);
        rubSystem.init();

        moneySystem.registerCommands();
        rubSystem.registerCommands();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Placeholders placeholders = new Placeholders(moneySystem, rubSystem);
            placeholders.register();
        }

    }

    @Override
    public void onDisable() {
        if (moneySystem != null) {
            moneySystem.shutdown();
        }
        if (rubSystem != null) {
            rubSystem.shutdown();
        }
    }
}