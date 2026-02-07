package mc.util.chat;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 02.01.2026
 */

@SuppressWarnings("all")
public class MessageUtil {
    @Getter
    private static String prefix = "&#30578Cɢ&#264877ʏ&#1B3A63-&#112B4Eᴄ&#112B4Eᴏ&#112B4Eʀ&#112B4Eᴇ &8» &f";

    private static final Map<UUID, PriorityData> currentPriority = new HashMap<>();
    private static final long PRIORITY_TIMEOUT = 1500;
    private static final Pattern HEX_PATTERN = Pattern.compile("&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

    private static class PriorityData {
        int priority;
        long timestamp;

        PriorityData(int priority) {
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static void sendUnknownPlayerMessage(CommandSender sender, String unkPlayer) {
        sender.sendMessage(colorize(prefix + "Игрок '&#30578C" + unkPlayer + "&f' не найден."));
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    public static void sendUsageMessage(CommandSender sender, String command) {
        sender.sendMessage(colorize(prefix + "Использование: &#30578C" + command));
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(prefix + message));
    }

    public static String getGYString(String message) {
        return colorize(prefix + message);
    }


    public static void sendActionBar(Player player, String message, boolean prefix) {
        sendActionBar(player, message, prefix, 1);
    }

    public static void sendActionBar(Player player, String message, boolean prefix, int priority) {
        UUID playerId = player.getUniqueId();

        PriorityData data = currentPriority.get(playerId);

        if (data != null) {
            if (System.currentTimeMillis() - data.timestamp > PRIORITY_TIMEOUT) {
                currentPriority.remove(playerId);
                data = null;
            } else if (data.priority > priority) {
                return;
            }
        }

        currentPriority.put(playerId, new PriorityData(priority));

        if (prefix) {
            player.sendActionBar(colorize(getPrefix() + message));
        } else {
            player.sendActionBar(colorize(message));
        }
    }

    public static void clearActionBar(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendActionBar(" ");
            currentPriority.remove(playerId);
        }
    }

    public static void clearActionBarIfPriority(UUID playerId, int priority) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            PriorityData data = currentPriority.get(playerId);
            if (data != null && data.priority == priority) {
                player.sendActionBar(" ");
                currentPriority.remove(playerId);
            }
        }
    }


    public static String colorize(String text) {
        if (text == null) return null;
        text = translateHexColors(text);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fullMatch = matcher.group();
            String hex = matcher.group(1);

            if (hex.length() == 3) {
                hex = String.format("%c%c%c%c%c%c",
                        hex.charAt(0), hex.charAt(0),
                        hex.charAt(1), hex.charAt(1),
                        hex.charAt(2), hex.charAt(2));
            }

            StringBuilder minecraftHex = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                minecraftHex.append('§').append(c);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(minecraftHex.toString()));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static String stripColors(String text) {
        if (text == null) return null;
        text = ChatColor.stripColor(text);
        text = text.replaceAll("§x(§[A-Fa-f0-9]){6}", "");
        text = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");

        return text;
    }

    public static boolean containsHexColors(String text) {
        return text != null && HEX_PATTERN.matcher(text).find();
    }

    public static String getPrefixColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String coloredText = colorize(text);
        StringBuilder prefixColors = new StringBuilder();

        for (int i = 0; i < coloredText.length(); i++) {
            char c = coloredText.charAt(i);

            if (c == '§' && i + 1 < coloredText.length()) {
                char nextChar = coloredText.charAt(i + 1);
                if ("0123456789abcdefklmnorxABCDEFKLMNORX".indexOf(nextChar) != -1) {
                    if (nextChar == 'x' || nextChar == 'X') {
                        if (i + 13 < coloredText.length()) {
                            prefixColors.append(coloredText, i, i + 14);
                            i += 13;
                            continue;
                        }
                    } else {
                        prefixColors.append(coloredText, i, i + 2);
                        i++;
                        continue;
                    }
                }
            }

            if (!Character.isWhitespace(c) && c != '§') {
                break;
            }

            if (c != '§') {
                prefixColors.append(c);
            }
        }

        return prefixColors.toString();
    }
}