package dansplugins.nomorecreepers;

import dansplugins.nomorecreepers.bstats.Metrics;
import dansplugins.nomorecreepers.eventhandlers.ExplosionHandler;
import dansplugins.nomorecreepers.eventhandlers.SpawnHandler;

import org.bukkit.plugin.java.JavaPlugin;

public class NoMoreCreepers extends JavaPlugin {
    private static NoMoreCreepers instance;
    private String version = "v0.4-alpha-1";
    public boolean allowSpawning = true;
    public boolean allowExploding = false;

    public static NoMoreCreepers getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // bStats
        int pluginId = 13432;
        Metrics metrics = new Metrics(this, pluginId);

        getServer().getPluginManager().registerEvents(new SpawnHandler(), this);
        getServer().getPluginManager().registerEvents(new ExplosionHandler(), this);


    }

    @Override
    public void onDisable() {

    }

    public boolean isSpawningAllowed() {
        return allowSpawning;
    }

    public boolean isExplodingAllowed() {
        return allowExploding;
    }
}