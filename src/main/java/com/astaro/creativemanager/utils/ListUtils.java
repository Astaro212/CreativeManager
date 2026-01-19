package com.astaro.creativemanager.utils;

import com.astaro.creativemanager.CreativeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import java.util.List;

public class ListUtils {

    private static final String WILDCARD = "*";

    /**
     * Проверяет, находится ли предмет/блок в списке (с учетом масок * и тегов #).
     *
     * @param search   имя материала для поиска (lowercase)
     * @param list     список из конфига
     * @param isWhitelist режим списка (true - whitelist, false - blacklist)
     * @return true, если действие должно быть ЗАПРЕЩЕНО (или разрешено, если это whitelist)
     */
    public static boolean inList(String search, List<String> list, boolean isWhitelist) {
        if (list == null || list.isEmpty()) return isWhitelist;

        for (String s : list) {
            s = s.toLowerCase();
            if (s.isEmpty()) continue;

            if (s.equals(WILDCARD)) return true;

            if (s.startsWith("#")) {
                if (checkTag(search, s.substring(1))) return true;
                continue;
            }

            if (s.contains(WILDCARD)) {
                if (checkWildcard(search, s)) return true;
                continue;
            }

            if (s.equals(search)) return true;
        }

        return false;
    }

    private static boolean checkWildcard(String search, String pattern) {
        if (pattern.startsWith(WILDCARD) && pattern.endsWith(WILDCARD)) {
            return search.contains(pattern.substring(1, pattern.length() - 1));
        }
        if (pattern.startsWith(WILDCARD)) {
            return search.endsWith(pattern.substring(1));
        }
        if (pattern.endsWith(WILDCARD)) {
            return search.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return false;
    }

    private static boolean checkTag(String materialName, String tagName) {
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) return false;

        NamespacedKey key = tagName.contains(":")
                ? NamespacedKey.fromString(tagName)
                : NamespacedKey.minecraft(tagName);

        if (key == null) return false;

        // Проверяем блоки
        Tag<Material> blockTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);
        if (blockTag != null && blockTag.isTagged(mat)) return true;

        // Проверяем предметы
        Tag<Material> itemTag = Bukkit.getTag(Tag.REGISTRY_ITEMS, key, Material.class);
        return itemTag != null && itemTag.isTagged(mat);
    }
}
