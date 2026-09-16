package dansplugins.nomorecreepers;

import dansplugins.nomorecreepers.bstats.Metrics;
import dansplugins.nomorecreepers.commands.ConfigCommand;
import dansplugins.nomorecreepers.commands.DefaultCommand;
import dansplugins.nomorecreepers.commands.HelpCommand;
import dansplugins.nomorecreepers.listeners.SpawnListener;
import dansplugins.nomorecreepers.services.ConfigService;
import dansplugins.nomorecreepers.trace.TraceClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.abs.PonderBukkitPlugin;
import preponderous.ponder.minecraft.bukkit.services.CommandService;
import preponderous.ponder.minecraft.bukkit.tools.EventHandlerRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NoMoreCreepers extends PonderBukkitPlugin {
    private final String pluginVersion = "v" + getDescription().getVersion();
    private final CommandService commandService = new CommandService(getPonder());
    private final ConfigService configService = new ConfigService(this);

    // A no-op until the config has been read, so a command arriving before
    // onEnable() finishes has something safe to report to.
    private TraceClient trace = TraceClient.disabled();

    @Override
    public void onEnable() {
        // bStats
        int pluginId = 13432;
        new Metrics(this, pluginId);

        initializeConfigFile();
        registerEventHandlers();
        initializeCommandService();

        initializeUsageReporting();
    }

    /**
     * Usage reporting: one event now, one per command; see config.yml. The
     * block is written to disk first if an upgraded server does not have it,
     * so the switch is where the console line says it is, and the outcome is
     * logged either way.
     */
    private void initializeUsageReporting() {
        configService.saveUsageReportingDefaultsIfNotPresent();
        trace = TraceClient.builder(configService.getUsageReportingEndpoint(), getName())
                .key(configService.getUsageReportingKey())
                .enabled(configService.isUsageReportingEnabled())
                .serverWideConfig(getDataFolder().getParentFile())
                .logger(getLogger())
                .build();
        if (trace.isEnabled()) {
            getLogger().info("Usage reporting is on: " + getName() + " sends its name, version and command names to "
                    + "https://trace.danielstephenson.dev - nothing about players or the server. Turn it off with "
                    + "usage-reporting.enabled: false in this plugin's config.yml, or for every plugin with "
                    + "enabled: false in plugins/trace/config.yml. "
                    + "Details: https://github.com/Stephenson-Software/trace#usage-reporting");
        } else {
            getLogger().info("Usage reporting is off (" + trace.disabledReason() + ").");
        }
        trace.report("startup", null, Collections.singletonMap("version", getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
        trace.close();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        trace.report("command", null, Collections.singletonMap("name", cmd.getName()));

        if (args.length == 0) {
            DefaultCommand defaultCommand = new DefaultCommand(this);
            return defaultCommand.execute(sender);
        }

        return commandService.interpretAndExecuteCommand(sender, label, args);
    }

    public String getVersion() {
        return pluginVersion;
    }
    public boolean isVersionMismatched() {
        String configVersion = this.getConfig().getString("version");
        if (configVersion == null || this.getVersion() == null) {
            return false;
        } else {
            return !configVersion.equalsIgnoreCase(this.getVersion());
        }
    }

    public boolean isSpawningAllowed() {
        return configService.getBoolean("allowSpawning");
    }

    private void initializeConfigFile() {
        if (!(new File("./plugins/NoMoreCreepers/config.yml").exists())) {
            configService.saveMissingConfigDefaultsIfNotPresent();
        }
        else {
            // pre load compatibility checks
            if (isVersionMismatched()) {
                configService.saveMissingConfigDefaultsIfNotPresent();
            }
            reloadConfig();
        }
    }

    private void registerEventHandlers() {
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(new SpawnListener(this));
        EventHandlerRegistry eventHandlerRegistry = new EventHandlerRegistry();
        eventHandlerRegistry.registerEventHandlers(listeners, this);
    }

    private void initializeCommandService() {
        ArrayList<AbstractPluginCommand> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(), new ConfigCommand(configService)
        ));
        commandService.initialize(commands, "That command wasn't found.");
    }
}