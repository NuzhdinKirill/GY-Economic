package mc.economy.rub;

import mc.GY;
import org.bukkit.OfflinePlayer;

import java.sql.*;

public class RubDB {
    private final GY plugin;
    private Connection connection;

    public RubDB(GY plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            String dbPath = plugin.getDataFolder().getAbsolutePath() + "/rub.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS balances (uuid TEXT PRIMARY KEY, balance REAL DEFAULT 0)");
                plugin.getLogger().info("БД рублей подключена: " + dbPath);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка подключения к БД рублей: " + e.getMessage());
        }
    }

    public boolean hasPlayer(OfflinePlayer player) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM balances WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public double getBalance(OfflinePlayer player) {
        // Всегда создаём запись если её нет (для онлайн игроков)
        if (player.isOnline()) {
            createIfMissing(player);
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT balance FROM balances WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("balance") : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public void createIfMissing(OfflinePlayer player) {
        if (hasPlayer(player)) return;

        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR IGNORE INTO balances (uuid, balance) VALUES (?, 0)")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void add(OfflinePlayer player, double amount) {
        if (amount <= 0) return;
        createIfMissing(player);

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE balances SET balance = balance + ? WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void take(OfflinePlayer player, double amount) {
        if (amount <= 0) return;
        createIfMissing(player);

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE balances SET balance = MAX(0, balance - ?) WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void set(OfflinePlayer player, double amount) {
        if (amount < 0) return;
        createIfMissing(player);

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE balances SET balance = ? WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void giveAllOnline(double amount) {
        if (amount <= 0) return;

        plugin.getServer().getOnlinePlayers().forEach(player -> {
            add(player, amount);
        });
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}