package Astrellipse.ActionTimer;

import Astrellipse.CustomMob.CustomMob;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionTimer extends BukkitRunnable{
    public static JavaPlugin plugin;
    CustomMob cm;
    public ActionTimer(JavaPlugin plugin, CustomMob cm) {
        ActionTimer.plugin = plugin;
        this.cm = cm;
    }

    @Override
    public void run() {
        if (!CustomMob.cMob) {
            return;
        }
        cm.cd();
    }
}
