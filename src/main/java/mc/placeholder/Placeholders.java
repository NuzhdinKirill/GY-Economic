package mc.placeholder;

import mc.core.GY;
import mc.core.utilites.data.PlayerData;
import mc.economy.money.MoneySystem;
import mc.economy.rub.RubSystem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "0";
        }

        GY.refreshPlayerData(offlinePlayer.getUniqueId());

        PlayerData playerData = GY.getPlayerData(offlinePlayer.getUniqueId());
        boolean modernEco = playerData.isModernEcoEnabled();

        String result = null;
        String currencySymbol = "$";

        if (params.equalsIgnoreCase("money")) {
            double balance = moneySystem.getDatabase().getBalance(offlinePlayer);
            result = formatBalance(balance, modernEco, currencySymbol);
        }
        else if (params.equalsIgnoreCase("rub")) {
            double balance = rubSystem.getDatabase().getBalance(offlinePlayer);
            currencySymbol = "❖";
            result = formatBalance(balance, modernEco, currencySymbol);
        }

        return result != null ? result : "0" + currencySymbol;
    }

    private String formatBalance(double value, boolean modern, String currency) {
        if (!modern || value < 1000) {
            return String.format("%.0f %s", value, currency);
        }

        if (value < 1_000_000) {
            double k = value / 1000.0;
            return formatShort(k, "тыс.");
        }
        else if (value < 1_000_000_000L) {
            double m = value / 1_000_000.0;
            return formatShort(m, "млн.");
        }
        else {
            double b = value / 1_000_000_000.0;
            return formatShort(b, "млрд.");
        }
    }

    private String formatShort(double num, String suffix) {
        double floored = Math.floor(num);

        if (num >= 10) {
            return String.format("%.0f %s", floored, suffix);
        }
        else {
            double withOneDecimal = Math.floor(num * 10) / 10.0;
            if (Math.abs(withOneDecimal - Math.floor(withOneDecimal)) < 0.000001) {
                return String.format("%.0f %s", withOneDecimal, suffix);
            } else {
                return String.format("%.1f %s", withOneDecimal, suffix);
            }
        }
    }
}