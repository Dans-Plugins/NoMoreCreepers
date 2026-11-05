package dansplugins.nomorecreepers;

import dansplugins.nomorecreepers.commands.ConfigCommand;
import dansplugins.nomorecreepers.commands.HelpCommand;
import dansplugins.nomorecreepers.services.ConfigService;
import dansplugins.nomorecreepers.trace.TraceClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.ponder.minecraft.bukkit.services.CommandService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Runs commands through the real ponder {@code CommandService}, built from the bundled
 * {@code plugin.yml} and given the plugin's own command table, where the other tests mock the
 * service or call a command directly. This is where the permission nodes are enforced: nothing
 * else stops any player turning creeper spawning on, and a command that lost its node would
 * still pass every other test.
 */
public class CommandRoutingTest {
    private static final String NOT_FOUND_MESSAGE = ChatColor.RED + "That command wasn't found.";
    private static final String HELP_LINE = ChatColor.AQUA + "/nmc help - View a list of helpful commands.";
    private static final String SUB_COMMAND_MESSAGE = ChatColor.RED + "Sub-commands: show, set";
    private static final String CONFIG_LIST_HEADER = ChatColor.AQUA + "=== Config List ===";

    private final NoMoreCreepers noMoreCreepers = mock(NoMoreCreepers.class);
    private final Player player = mock(Player.class);
    private FileConfiguration config;

    @BeforeEach
    public void setUp() throws Exception {
        config = new YamlConfiguration();
        config.set("version", "v1.3.2");
        config.set("allowSpawning", false);
        when(noMoreCreepers.getConfig()).thenReturn(config);
        when(noMoreCreepers.getVersion()).thenReturn("v1.3.2");
        when(noMoreCreepers.isSpawningAllowed()).thenCallRealMethod();
        when(noMoreCreepers.onCommand(any(), any(), anyString(), any())).thenCallRealMethod();

        // A mock skips the field initializers, so the collaborators the real methods
        // reach for are put in place by hand.
        setField("configService", new ConfigService(noMoreCreepers));
        setField("commandService", new CommandService(declaredCommands().getCommands().keySet()));
        setField("trace", TraceClient.disabled());

        Method initializeCommandService = NoMoreCreepers.class.getDeclaredMethod("initializeCommandService");
        initializeCommandService.setAccessible(true);
        initializeCommandService.invoke(noMoreCreepers);
    }

    private void setField(String name, Object value) throws ReflectiveOperationException {
        Field field = NoMoreCreepers.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(noMoreCreepers, value);
    }

    private PluginDescriptionFile declaredCommands() throws IOException, InvalidDescriptionException {
        try (InputStream in = pluginYml()) {
            return new PluginDescriptionFile(in);
        }
    }

    private InputStream pluginYml() {
        return getClass().getClassLoader().getResourceAsStream("plugin.yml");
    }

    private void grant(String permission) {
        when(player.hasPermission(permission)).thenReturn(true);
    }

    private boolean run(String name, String label, String... args) {
        Command command = mock(Command.class);
        when(command.getName()).thenReturn(name);
        return noMoreCreepers.onCommand(player, command, label, args);
    }

    private static String permissionMessage(String permission) {
        return ChatColor.RED + "In order to use this command, you need one of the following permission: '["
                + permission + "]'";
    }

    @Test
    public void bareCommandNeedsNoPermission() {
        assertTrue(run("nmc", "nmc"));

        verify(player).sendMessage(ChatColor.AQUA + "NoMoreCreepers v1.3.2");
        verify(player, never()).hasPermission(anyString());
    }

    @Test
    public void helpRunsWithNmcHelp() {
        grant("nmc.help");

        assertTrue(run("nmc", "nmc", "help"));

        verify(player).sendMessage(HELP_LINE);
    }

    @Test
    public void helpIsRefusedWithoutNmcHelp() {
        grant("nmc.config");

        assertFalse(run("nmc", "nmc", "help"));

        verify(player).sendMessage(permissionMessage("nmc.help"));
        verify(player, never()).sendMessage(HELP_LINE);
    }

    @Test
    public void bareConfigListsSubCommandsWithNmcConfig() {
        grant("nmc.config");

        assertFalse(run("nmc", "nmc", "config"));

        verify(player).sendMessage(SUB_COMMAND_MESSAGE);
    }

    @Test
    public void bareConfigIsRefusedWithoutNmcConfig() {
        grant("nmc.help");

        assertFalse(run("nmc", "nmc", "config"));

        verify(player).sendMessage(permissionMessage("nmc.config"));
        verify(player, never()).sendMessage(SUB_COMMAND_MESSAGE);
    }

    @Test
    public void configShowRunsWithNmcConfig() {
        grant("nmc.config");

        assertTrue(run("nmc", "nmc", "config", "show"));

        verify(player).sendMessage(CONFIG_LIST_HEADER);
    }

    @Test
    public void configShowIsRefusedWithoutNmcConfig() {
        grant("nmc.help");

        assertFalse(run("nmc", "nmc", "config", "show"));

        verify(player).sendMessage(permissionMessage("nmc.config"));
        verify(player, never()).sendMessage(CONFIG_LIST_HEADER);
    }

    /** A player granted only nmc.help must not be able to let creepers back in. */
    @Test
    public void configSetIsRefusedWithoutNmcConfigAndChangesNothing() {
        grant("nmc.help");

        assertFalse(run("nmc", "nmc", "config", "set", "allowSpawning", "true"));

        verify(player).sendMessage(permissionMessage("nmc.config"));
        assertFalse(noMoreCreepers.isSpawningAllowed());
        verify(noMoreCreepers, never()).saveConfig();
    }

    @Test
    public void configSetWithNmcConfigAllowsSpawningAtOnce() {
        grant("nmc.config");

        assertTrue(run("nmc", "nmc", "config", "set", "allowSpawning", "true"));

        verify(player).sendMessage(ChatColor.GREEN + "Boolean set.");
        assertTrue(noMoreCreepers.isSpawningAllowed());
        verify(noMoreCreepers).saveConfig();
    }

    @Test
    public void bothDeclaredCommandsRunSubCommands() {
        grant("nmc.help");

        assertTrue(run("nmc", "nmc", "help"));
        assertTrue(run("nomorecreepers", "nomorecreepers", "help"));

        verify(player, times(2)).sendMessage(HELP_LINE);
    }

    /**
     * Bukkit keeps the namespaced labels registered, and they are the ones left if other
     * plugins take both short names. The command service ignores them, with no reply at
     * all, if it is given the label rather than the registered name. This goes through
     * Bukkit's own command map, so the label and name are the ones Bukkit passes. The map
     * reports true either way, so the reply is what shows the sub-command ran.
     */
    @Test
    public void namespacedLabelsRunSubCommandsThroughBukkitsCommandMap() throws Exception {
        grant("nmc.help");
        PluginDescriptionFile description = declaredCommands();
        Plugin plugin = mock(Plugin.class);
        when(plugin.getDescription()).thenReturn(description);
        when(plugin.isEnabled()).thenReturn(true);
        List<Command> commands = PluginCommandYamlParser.parse(plugin);
        for (Command command : commands) {
            ((PluginCommand) command).setExecutor(noMoreCreepers);
        }

        // Dispatching looks up the timings setting on the static server.
        Server server = mock(Server.class, RETURNS_DEEP_STUBS);
        Field bukkitServer = Bukkit.class.getDeclaredField("server");
        bukkitServer.setAccessible(true);
        bukkitServer.set(null, server);
        try {
            SimpleCommandMap commandMap = new SimpleCommandMap(server);
            commandMap.registerAll(description.getName(), commands);

            commandMap.dispatch(player, "nomorecreepers:nmc help");
            commandMap.dispatch(player, "nomorecreepers:nomorecreepers help");
        } finally {
            bukkitServer.set(null, null);
        }

        verify(player, times(2)).sendMessage(HELP_LINE);
    }

    @Test
    public void namespacedLabelsStillNeedThePermission() {
        grant("nmc.help");

        assertFalse(run("nmc", "nomorecreepers:nmc", "config", "set", "allowSpawning", "true"));
        assertFalse(run("nomorecreepers", "nomorecreepers:nomorecreepers", "config", "set", "allowSpawning", "true"));

        verify(player, times(2)).sendMessage(permissionMessage("nmc.config"));
        assertFalse(noMoreCreepers.isSpawningAllowed());
        verify(noMoreCreepers, never()).saveConfig();
    }

    /** No permission is checked for a name that matches no command. */
    @Test
    public void unknownSubCommandIsReportedWhateverThePermissions() {
        assertFalse(run("nmc", "nmc", "reload"));

        verify(player).sendMessage(NOT_FOUND_MESSAGE);
        verify(player, never()).hasPermission(anyString());
    }

    /** Unlike {@code show} and {@code set}, which ConfigCommand matches in any case. */
    @Test
    public void subCommandNamesAreCaseSensitive() {
        grant("nmc.help");
        grant("nmc.config");

        assertFalse(run("nmc", "nmc", "HELP"));
        assertFalse(run("nmc", "nmc", "Config", "show"));

        verify(player, times(2)).sendMessage(NOT_FOUND_MESSAGE);
    }

    /** Ponder only checks the node a command names; only plugin.yml gives it a default. */
    @Test
    public void eachCommandsNodeIsDeclaredInPluginYmlWithItsDocumentedDefault() throws IOException {
        YamlConfiguration pluginYml;
        try (InputStreamReader reader = new InputStreamReader(pluginYml(), StandardCharsets.UTF_8)) {
            pluginYml = YamlConfiguration.loadConfiguration(reader);
        }

        assertEquals(Collections.singletonList("nmc.help"), new HelpCommand().getPermissions());
        assertEquals(Collections.singletonList("nmc.config"),
                new ConfigCommand(mock(ConfigService.class)).getPermissions());
        assertEquals("true", pluginYml.getString("permissions.nmc.help.default"));
        assertEquals("op", pluginYml.getString("permissions.nmc.config.default"));
    }
}
