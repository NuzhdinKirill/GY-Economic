package mc.utilites.cmd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 02.01.2026
 */

public class FilterUtil {

    public static List<String> filterCompletions(List<String> options, String input) {
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(option);
            }
        }
        return filtered;
    }

}