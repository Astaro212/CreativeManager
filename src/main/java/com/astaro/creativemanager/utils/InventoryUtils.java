package com.astaro.creativemanager.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class InventoryUtils {

    /**
     * Преобразует массив ItemStack в строку Base64.
     * В 1.21.4 корректно сохраняет все Data Components.
     */
    public static String itemStackArrayToBase64(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка при сериализации инвентаря", e);
        }
    }

    /**
     * Восстанавливает массив ItemStack из строки Base64.
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        if (data == null || data.isEmpty()) return new ItemStack[0];

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Неизвестный класс при десериализации инвентаря", e);
        }
    }
}
