package dansplugins.nomorecreepers;

import dansplugins.nomorecreepers.bstats.Metrics;
import dansplugins.nomorecreepers.commands.ConfigCommand;
import dansplugins.nomorecreepers.commands.DefaultCommand;
import dansplugins.nomorecreepers.commands.HelpCommand;
import dansplugins.nomorecreepers.eventhandlers.SpawnHandler;
import dansplugins.nomorecreepers.services.LocalConfigService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import preponderous.ponder.minecraft.bukkit.PonderMC;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.abs.PonderBukkitPlugin;
import preponderous.ponder.minecraft.bukkit.services.CommandService;
import preponderous.ponder.minecraft.bukkit.tools.EventHandlerRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class NoMoreCreepers extends PonderBukkitPlugin {
    private static NoMoreCreepers instance;
    private final String pluginVersion = "v" + getDescription().getVersion();
    private final CommandService commandService = new CommandService((PonderMC) getPonder());

    public static NoMoreCreepers getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // bStats
        int pluginId = 13432;
        new Metrics(this, pluginId);

        initializeConfigFile();
        registerEventHandlers();
        initializeCommandService();
    }

    @Override
    public void onDisable() {

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            DefaultCommand defaultCommand = new DefaultCommand();
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
        return LocalConfigService.getInstance().getBoolean("allowSpawning");
    }

    private void initializeConfigFile() {
        if (!(new File("./plugins/NoMoreCreepers/config.yml").exists())) {
            LocalConfigService.getInstance().saveMissingConfigDefaultsIfNotPresent();
        }
        else {
            // pre load compatibility checks
            if (isVersionMismatched()) {
                LocalConfigService.getInstance().saveMissingConfigDefaultsIfNotPresent();
            }
            reloadConfig();
        }
    }

    private void registerEventHandlers() {
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(new SpawnHandler());
        EventHandlerRegistry eventHandlerRegistry = new EventHandlerRegistry();
        eventHandlerRegistry.registerEventHandlers(listeners, this);
    }

    private void initializeCommandService() {
        ArrayList<AbstractPluginCommand> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(), new ConfigCommand()
        ));
        commandService.initialize(commands, "That command wasn't found.");
    }
}