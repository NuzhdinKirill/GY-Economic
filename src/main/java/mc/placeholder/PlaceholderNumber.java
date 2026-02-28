package mc.placeholder;

import mc.core.GY;
import mc.core.utilites.data.PlayerData;
import mc.economy.money.MoneySystem;
import mc.economy.rub.RubSystem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderNumber extends PlaceholderExpansion {

    private final MoneySystem moneySystem;
    private final RubSystem rubSystem;

    public PlaceholderNumber(MoneySystem moneySystem, RubSystem rubSystem) {
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

        String result = null;

        if (params.equalsIgnoreCase("money")) {
            double balance = moneySystem.getDatabase().getBalance(offlinePlayer);
            result = String.valueOf(balance);
        }
        else if (params.equalsIgnoreCase("rub")) {
            double balance = rubSystem.getDatabase().getBalance(offlinePlayer);
            result = String.valueOf(balance);
        }

        return result != null ? result : "0";
    }
}