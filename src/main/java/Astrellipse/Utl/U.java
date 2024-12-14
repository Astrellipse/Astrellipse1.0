package Astrellipse.Utl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class U {
    //アイテム渡す
    public static void addItem(Player player,ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(),item);
        } else {
            player.getInventory().addItem(item);
        }
    }
}
