package mc;

import mc.economy.money.MoneySystem;
import mc.economy.rub.RubSystem;
import mc.placeholder.Placeholders;
import org.bukkit.plugin.java.JavaPlugin;

public final class GY extends JavaPlugin {
    private MoneySystem moneySystem;
    private RubSystem rubSystem;

    @Override
    public void onEnable() {
        // Создаём папку для данных
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Инициализация системы монет
        moneySystem = new MoneySystem(this);
        moneySystem.init();

        // Инициализация системы рублей
        rubSystem = new RubSystem(this);
        rubSystem.init();

        // Регистрация команд
        moneySystem.registerCommands();
        rubSystem.registerCommands();

        // Регистрация PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Placeholders placeholders = new Placeholders(moneySystem, rubSystem);
            placeholders.register();
            getLogger().info("PlaceholderAPI зарегистрирован!");
        }

        getLogger().info("GY-Economy включен!");
    }

    @Override
    public void onDisable() {
        if (moneySystem != null) {
            moneySystem.shutdown();
        }
        if (rubSystem != null) {
            rubSystem.shutdown();
        }
        getLogger().info("GY-Economy выключен!");
    }

    public MoneySystem getMoneySystem() {
        return moneySystem;
    }

    public RubSystem getRubSystem() {
        return rubSystem;
    }
}