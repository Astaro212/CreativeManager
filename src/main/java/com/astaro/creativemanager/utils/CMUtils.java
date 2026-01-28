package com.astaro.creativemanager.utils;

import com.astaro.creativemanager.CreativeManager;
import fr.k0bus.k0buscore.utils.StringUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class CMUtils {
    public static LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public static void sendMessage(CommandSender messageTo, String text) {
        String langMsg = CreativeManager.getLang().getString(text);
        if (langMsg != null && !langMsg.isEmpty()) {
            messageTo.sendMessage(serializer.deserialize(parse(StringUtils.parse(langMsg))));
        }
    }

    public static void sendMessage(CommandSender messageTo, String text, Map<String, String> replaceMap) {
        String langMsg = CreativeManager.getLang().getString(text);
        if (langMsg != null && !langMsg.isEmpty()) {
            messageTo.sendMessage(serializer.deserialize(parse(StringUtils.parse(langMsg), replaceMap)));
        }
    }

    public static void sendRawMessage(CommandSender messageTo, String text) {
        if (text != null && !text.isEmpty()) {
            messageTo.sendMessage(parse(StringUtils.parse(text)));
        }
    }

    public static String parse(String string) {
        return string.replace("{TAG}", CreativeManager.TAG);
    }

    // Логика замены (теперь работает с любым типом Map)
    public static String parse(String string, Map<String, String> replaceMap) {
        string = parse(string);
        if (replaceMap == null) return string;

        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }
}
