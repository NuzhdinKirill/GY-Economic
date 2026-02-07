package mc.placeholder;

import mc.economy.money.MoneySystem;
import mc.economy.rub.RubSystem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
    private final MoneySystem moneySystem;
    private final RubSystem rubSystem;

    public Placeholders(MoneySystem moneySystem, RubSystem rubSystem) {
        this.moneySystem = moneySystem;
        this.rubSystem = rubSystem;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "gy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GY-Core";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        if (params.equalsIgnoreCase("money")) {
            double balance = moneySystem.getDatabase().getBalance(player);
            return String.format("%.0f", balance);
        }

        if (params.equalsIgnoreCase("rub")) {
            double balance = rubSystem.getDatabase().getBalance(player);
            return String.format("%.0f", balance);
        }

        return null;
    }
}