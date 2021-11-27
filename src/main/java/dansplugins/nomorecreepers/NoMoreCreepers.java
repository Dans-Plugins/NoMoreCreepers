package dansplugins.nomorecreepers;

import dansplugins.nomorecreepers.bstats.Metrics;
import dansplugins.nomorecreepers.commands.ConfigCommand;
import dansplugins.nomorecreepers.commands.DefaultCommand;
import dansplugins.nomorecreepers.commands.HelpCommand;
import dansplugins.nomorecreepers.eventhandlers.ExplosionHandler;
import dansplugins.nomorecreepers.eventhandlers.SpawnHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import preponderous.ponder.AbstractPonderPlugin;
import preponderous.ponder.misc.PonderAPI_Integrator;
import preponderous.ponder.misc.specification.ICommand;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NoMoreCreepers extends AbstractPonderPlugin {
    private static NoMoreCreepers instance;
    private String version = "v0.4";

    public static NoMoreCreepers getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // bStats
        int pluginId = 13432;
        Metrics metrics = new Metrics(this, pluginId);

        ponderAPI_integrator = new PonderAPI_Integrator(this);
        toolbox = getPonderAPI().getToolbox();
        initializeConfigService();
        initializeConfigFile();
        registerEventHandlers();
        initializeCommandService();
        getPonderAPI().setDebug(false);
    }

    @Override
    public void onDisable() {

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            DefaultCommand defaultCommand = new DefaultCommand();
            return defaultCommand.execute(sender);
        }

        return getPonderAPI().getCommandService().interpretCommand(sender, label, args);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isVersionMismatched() {
        String configVersion = this.getConfig().getString("version");
        if (configVersion == null || this.getVersion() == null) {
            return false;
        } else {
            return !configVersion.equalsIgnoreCase(this.getVersion());
        }
    }

    public boolean isSpawningAllowed() {
        return getPonderAPI().getConfigService().getBoolean("allowSpawning");
    }

    public boolean isExplodingAllowed() {
        return getPonderAPI().getConfigService().getBoolean("allowExploding");
    }

    private void initializeConfigService() {
        HashMap<String, Object> configOptions = new HashMap<>();
        configOptions.put("debugMode", false);
        configOptions.put("allowSpawning", false);
        configOptions.put("allowExploding", false);
        getPonderAPI().getConfigService().initialize(configOptions);
    }

    private void initializeConfigFile() {
        if (!(new File("./plugins/NoMoreCreepers/config.yml").exists())) {
            getPonderAPI().getConfigService().saveMissingConfigDefaultsIfNotPresent();
        }
        else {
            // pre load compatibility checks
            if (isVersionMismatched()) {
                getPonderAPI().getConfigService().saveMissingConfigDefaultsIfNotPresent();
            }
            reloadConfig();
        }
    }

    private void registerEventHandlers() {
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(new SpawnHandler());
        listeners.add(new ExplosionHandler());
        getToolbox().getEventHandlerRegistry().registerEventHandlers(listeners, this);
    }

    private void initializeCommandService() {
        ArrayList<ICommand> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(), new ConfigCommand()
        ));
        getPonderAPI().getCommandService().initialize(commands, "That command wasn't found.");
    }
}