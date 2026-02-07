package mc.economy.rub;

import mc.GY;
import mc.utilites.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RubCommand implements TabExecutor {
    private final RubSystem rubSystem;

    public RubCommand(GY plugin, RubSystem rubSystem) {
        this.rubSystem = rubSystem;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        String command = cmd.getName().toLowerCase();

        if (command.equals("rubpay")) {
            return handlePay(sender, args);
        }

        if (command.equals("rub")) {
            return handleRub(sender, args);
        }

        return false;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "Команду может использовать только игрок.");
            return true;
        }

        if (!sender.hasPermission("gy.economy.rub")) {
            MessageUtil.sendMessage(player, "Нет прав");
            return true;
        }

        if (args.length != 2) {
            MessageUtil.sendUsageMessage(player, "/rubpay [Игрок] [Сумма]");
            return true;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(player.getName())) {
            MessageUtil.sendMessage(player, "Нельзя перевести себе");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            MessageUtil.sendUnknownPlayerMessage(player, targetName);
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                MessageUtil.sendMessage(player, "Сумма должна быть больше 0");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(player, "Неверная сумма");
            return true;
        }

        RubDB db = rubSystem.getDatabase();
        double balance = db.getBalance(player);

        if (balance < amount) {
            MessageUtil.sendMessage(player, "Недостаточно руб");
            return true;
        }

        db.take(player, amount);
        db.add(target, amount);

        MessageUtil.sendMessage(player, "Вы отправили &#30578C" + amount + "❖ &fруб игроку &#30578C" + target.getName());
        MessageUtil.sendMessage(target, "Вы получили &#30578C" + amount + "❖ &fруб от &#30578C" + player.getName());
        return true;
    }

    private boolean handleRub(CommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendUsageMessage(sender, "/rub [info/take/give/giveall]");
            return true;
        }

        String sub = args[0].toLowerCase();
        RubDB db = rubSystem.getDatabase();

        if (sub.equals("info") && args.length == 2) {
            if (!sender.hasPermission("gy.economy.admin")) {
                MessageUtil.sendMessage(sender, "Нет прав");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!db.hasPlayer(target)) {
                MessageUtil.sendUnknownPlayerMessage(sender, args[1]);
                return true;
            }

            double balance = db.getBalance(target);
            MessageUtil.sendMessage(sender, "Баланс игрока &#30578C" + args[1] + " &f: &#30578C" + balance + "❖ &fруб");
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
                MessageUtil.sendMessage(sender, "На сервере нет игроков");
                return true;
            }

            db.giveAllOnline(amount);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                MessageUtil.sendMessage(onlinePlayer, "Вам выдано &#30578C" + amount + "❖ &fруб");
            }

            MessageUtil.sendMessage(sender, "Выдано &#30578C" + amount + "❖ &fруб &#30578C" + count + " &fигрокам");
            return true;
        }

        if (args.length != 3) {
            MessageUtil.sendUsageMessage(sender, "/rub [give/take/set]");
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
        if (!db.hasPlayer(target)) {
            MessageUtil.sendUnknownPlayerMessage(sender, targetName);
            return true;
        }

        switch (sub) {
            case "give" -> {
                if (amount <= 0) {
                    MessageUtil.sendMessage(sender, "Сумма должна быть больше 0");
                    return true;
                }
                db.add(target, amount);
                MessageUtil.sendMessage(sender, "Баланс игрока &#30578C" + targetName + " &fпополнен на &#30578C" + amount + "❖ &fруб");
                if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                    MessageUtil.sendMessage(targetPlayer, "Вам выдано &#30578C" + amount + "❖ &fруб");
                }
            }
            case "take" -> {
                if (amount <= 0) {
                    MessageUtil.sendMessage(sender, "Сумма должна быть больше 0");
                    return true;
                }
                double current = db.getBalance(target);
                if (current < amount) {
                    MessageUtil.sendMessage(sender, "Недостаточно руб у игрока. Баланс: &#30578C" + current + "❖ &fруб");
                    return true;
                }
                db.take(target, amount);
                MessageUtil.sendMessage(sender, "С баланса игрока &#30578C" + targetName + " списано &#30578C" + amount + "❖ &fруб");
                if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                    MessageUtil.sendMessage(targetPlayer, "С вашего баланса списано &#30578C" + amount + "❖ &fруб");
                }
            }
            case "set" -> {
                if (amount < 0) {
                    MessageUtil.sendMessage(sender, "Сумма должна быть больше 0");
                    return true;
                }
                db.set(target, amount);
                MessageUtil.sendMessage(sender, "Баланс игрока &#30578C" + targetName + " &fустановлен на &#30578C" + amount + "❖ &fруб");
                if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                    MessageUtil.sendMessage(targetPlayer, "Ваш баланс установлен на &#30578C" + amount + "❖ &fруб");
                }
            }
            default -> {
                MessageUtil.sendUsageMessage(sender, "/rub [give/take/set]");
                return false;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();
        String command = cmd.getName().toLowerCase();

        if (args.length == 1) {
            if (command.equals("rubpay")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!(sender instanceof Player s && p.getName().equalsIgnoreCase(s.getName()))) {
                        completions.add(p.getName());
                    }
                });
            } else if (command.equals("rub") && sender.hasPermission("gy.economy.admin")) {
                completions.add("give");
                completions.add("take");
                completions.add("set");
                completions.add("giveall");
                completions.add("info");
            }
        } else if (args.length == 2 && command.equals("rub")) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set") || sub.equals("info")) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
        }

        if (!completions.isEmpty() && !args[args.length - 1].isEmpty()) {
            String input = args[args.length - 1].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        }

        return completions;
    }
}
