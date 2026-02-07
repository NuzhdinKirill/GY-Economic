package mc.economy.money;

import mc.GY;
import mc.util.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MoneyCommand implements TabExecutor {
    private final MoneySystem moneySystem;

    public MoneyCommand(GY plugin, MoneySystem moneySystem) {
        this.moneySystem = moneySystem;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        String command = cmd.getName().toLowerCase();

        if (command.equals("pay")) {
            return handlePay(sender, args);
        }

        if (command.equals("money")) {
            return handleMoney(sender, args);
        }

        return false;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!sender.hasPermission("gy.economy.money")) {
            MessageUtil.sendMessage(player, "Нет прав!");
            return true;
        }

        if (args.length != 2) {
            MessageUtil.sendMessage(player, "Использование: /pay <игрок> <сумма>");
            return true;
        }

        String targetName = args[0];

        if (targetName.equalsIgnoreCase(sender.getName())) {
            MessageUtil.sendMessage(player, "Нельзя перевести себе");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            MessageUtil.sendMessage(player, "Игрок не найден");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                MessageUtil.sendMessage(player, "Сумма должно быть больше 0");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(player, "Неверная сумма");
            return true;
        }

        MoneyDB db = moneySystem.getDatabase();

        double balance = db.getBalance(player);
        if (balance < amount) {
            MessageUtil.sendMessage(player, "Недостаточно монет");
            return true;
        }

        db.take(player, amount);
        db.add(target, amount);

        MessageUtil.sendMessage(player, "Вы отправили &#30578C" + amount + " &fмонет &#30578C" + target.getName());
        MessageUtil.sendMessage(target, "Вы получили &#30578C" + amount + " &fмонет от &#30578C" + player.getName());
        return true;
    }

    private boolean handleMoney(CommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(sender, "Использование: /money <info/take/give/giveall>");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("info") && args.length == 2) {
            if (!sender.hasPermission("gy.economy.admin")) {
                MessageUtil.sendMessage(sender, "Нет прав");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            MoneyDB db = moneySystem.getDatabase();

            if (!db.hasPlayer(target)) {
                MessageUtil.sendMessage(sender, "Игрок никогда не заходил на сервер");
                return true;
            }

            double balance = db.getBalance(target);
            MessageUtil.sendMessage(sender, "Баланс &#30578C" + args[1] + " &fсоставляет &#30578C" + balance + " &fмонет");
            return true;
        }

        if (!sender.hasPermission("gy.economy.admin")) {
            MessageUtil.sendMessage(sender, "Нет прав");
            return true;
        }

        if (sub.equals("giveall") && args.length == 2) {
            double amount;
            try {
                amount = Double.parseDouble(args[1]);
                if (amount <= 0) {
                    MessageUtil.sendMessage(sender, "Сумма должна быть больше 0");
                    return true;
                }
            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(sender, "Неверная сумма");
                return true;
            }

            int count = Bukkit.getOnlinePlayers().size();
            if (count == 0) {
                MessageUtil.sendMessage(sender, "На сервере нет игроков!");
                return true;
            }

            MoneyDB db = moneySystem.getDatabase();
            db.giveAllOnline(amount);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                MessageUtil.sendMessage(onlinePlayer, "Вам выдано &#30578C" + amount + " &fмонет!");
            }

            MessageUtil.sendMessage(sender, "Выдано &#30578C" + amount + " &fмонет &#30578C" + count + " &fигрокам");
            return true;
        }

        if (args.length != 3) {
            return true;
        }

        String targetName = args[1];
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(sender, "Неверная сумма");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        MoneyDB db = moneySystem.getDatabase();

        if (!db.hasPlayer(target)) {
            MessageUtil.sendMessage(sender, "Игрок никогда не заходил на сервер");
            return true;
        }

        switch (sub) {
            case "give":
                if (amount <= 0) {
                    MessageUtil.sendMessage(sender, "Сумма должна быть больше 0");
                    return true;
                }
                db.add(target, amount);
                MessageUtil.sendMessage(sender, "Вы выдали &#30578C" + targetName + "&f: &#30578C" + amount + " &fмонет");

                if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                    MessageUtil.sendMessage(targetPlayer, "Вам выдали &#30578C" + amount + " &fмонет");
                }
                break;

            case "take":
                if (amount <= 0) {
                    MessageUtil.sendMessage(sender, "Сумма должна быть больше 0");
                    return true;
                }
                double current = db.getBalance(target);
                if (current < amount) {
                    MessageUtil.sendMessage(sender, "У игрока недостаточно монет! У него: &#30578C" + current + " &fмонет" );
                    return true;
                }
                db.take(target, amount);
                MessageUtil.sendMessage(sender, "Вы забрали у &#30578C" + targetName + "&f: &#30578C" + amount + " &fмонет");

                if (target instanceof Player targetPlayer && targetPlayer.isOnline() && sender instanceof Player) {
                    MessageUtil.sendMessage(targetPlayer, "У вас забрали &#30578C" + amount + " &fмонет");
                }
                break;

            case "set":
                if (amount < 0) {
                    MessageUtil.sendMessage(sender, "Сумма должна быть больше 0");
                    return true;
                }
                db.set(target, amount);
                MessageUtil.sendMessage(sender, "Вы установили &#30578C" + targetName + "&f: &#30578C" + amount + " &fмонет");

                if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                    MessageUtil.sendMessage(targetPlayer, "Вам установили &#30578C" + amount + " &fмонет");
                }
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();
        String command = cmd.getName().toLowerCase();

        if (args.length == 1) {
            if (command.equals("pay")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!(sender instanceof Player) || !p.getName().equals(sender.getName())) {
                        completions.add(p.getName());
                    }
                });
            } else if (command.equals("money") && sender.hasPermission("gy.economy.admin")) {
                completions.add("give");
                completions.add("take");
                completions.add("set");
                completions.add("giveall");
                completions.add("info");
            }
        } else if (args.length == 2 && command.equals("money")) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set") || sub.equals("info")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    completions.add(p.getName());
                });
            }
        }

        if (!completions.isEmpty() && !args[args.length - 1].isEmpty()) {
            String input = args[args.length - 1].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        }

        return completions;
    }
}