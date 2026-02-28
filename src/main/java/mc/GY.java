package mc;

import lombok.Getter;
import mc.economy.money.MoneySystem;
import mc.economy.rub.RubSystem;
import mc.north.utilites.chat.MessageUtil;
import mc.placeholder.PlaceholderNumber;
import mc.placeholder.PlaceholderVisual;
import org.bukkit.plugin.java.JavaPlugin;

public final class GY extends JavaPlugin {
    @Getter
    private static GY instance;
    @Getter
    private static MoneySystem moneySystem;
    @Getter
    private static RubSystem rubSystem;
    @Getter
    public static MessageUtil msg;

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
        msg = new MessageUtil("&#30578C&lɴ&#284C7D&lᴏ&#21416D&lʀ&#19365E&lᴛ&#112B4E&lʜ &8» &f", "&#30578C");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderNumber placeholders = new PlaceholderNumber(moneySystem, rubSystem);
            PlaceholderVisual placeholderVisual = new PlaceholderVisual(moneySystem, rubSystem);
            placeholders.register();
            placeholderVisual.register();
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