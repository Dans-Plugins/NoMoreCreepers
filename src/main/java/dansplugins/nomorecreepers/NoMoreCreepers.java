package dansplugins.nomorecreepers;

import dansplugins.nomorecreepers.bstats.Metrics;
import dansplugins.nomorecreepers.eventhandlers.SpawnHandler;

import org.bukkit.plugin.java.JavaPlugin;

public class NoMoreCreepers extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new SpawnHandler(), this);

        // bStats
        int pluginId = 13432;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {

    }
}
