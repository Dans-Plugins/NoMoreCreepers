package dansplugins.nomorecreepers;

import com.sun.net.httpserver.HttpServer;
import dansplugins.nomorecreepers.services.ConfigService;
import dansplugins.nomorecreepers.trace.TraceClient;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import preponderous.ponder.minecraft.bukkit.services.CommandService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Characterizes the plugin class's own logic: the version check that decides, on startup,
 * whether an existing config.yml gets the missing-defaults pass; the {@code allowSpawning}
 * lookup the spawn listener relies on; how {@code onCommand} routes a command and what
 * usage event it reports for it; and that {@code onDisable} sends a usage count still open.
 * A false negative in the version check leaves a new option
 * off disk after an upgrade; a false positive rewrites the version on every start.
 */
public class NoMoreCreepersTest {
    private final NoMoreCreepers noMoreCreepers = mock(NoMoreCreepers.class);
    // A player, because only commands a player runs report a usage event.
    private final CommandSender sender = mock(Player.class);
    private final Command command = mock(Command.class);
    private final CommandService commandService = mock(CommandService.class);
    private final Logger logger = mock(Logger.class);
    private FileConfiguration config;

    @TempDir
    File pluginsDirectory;

    @BeforeEach
    public void setUp() throws ReflectiveOperationException {
        config = new YamlConfiguration();
        when(noMoreCreepers.getConfig()).thenReturn(config);
        when(noMoreCreepers.getVersion()).thenReturn("v1.3.2");
        when(noMoreCreepers.isVersionMismatched()).thenCallRealMethod();
        when(noMoreCreepers.isSpawningAllowed()).thenCallRealMethod();
        when(noMoreCreepers.onCommand(any(), any(), anyString(), any())).thenCallRealMethod();
        when(command.getName()).thenReturn("nmc");

        // A mock skips the field initializers, so the collaborators the real methods
        // reach for are put in place by hand.
        setField("configService", new ConfigService(noMoreCreepers));
        setField("commandService", commandService);
        setField("trace", TraceClient.disabled());
    }

    private void setField(String name, Object value) throws ReflectiveOperationException {
        Field field = NoMoreCreepers.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(noMoreCreepers, value);
    }

    @Test
    public void matchingVersionIsNotAMismatch() {
        config.set("version", "v1.3.2");

        assertFalse(noMoreCreepers.isVersionMismatched());
    }

    @Test
    public void differentVersionIsAMismatch() {
        config.set("version", "v1.3.1");

        assertTrue(noMoreCreepers.isVersionMismatched());
    }

    @Test
    public void versionsDifferingOnlyInCaseAreNotAMismatch() {
        config.set("version", "V1.3.2");

        assertFalse(noMoreCreepers.isVersionMismatched());
    }

    /**
     * A file with no version line is not treated as a mismatch, so on its own it does not
     * trigger the defaults pass. This pins the current behaviour rather than endorsing it.
     */
    @Test
    public void missingConfigVersionIsNotAMismatch() {
        assertFalse(noMoreCreepers.isVersionMismatched());
    }

    @Test
    public void missingPluginVersionIsNotAMismatch() {
        config.set("version", "v1.3.1");
        when(noMoreCreepers.getVersion()).thenReturn(null);

        assertFalse(noMoreCreepers.isVersionMismatched());
    }

    @Test
    public void spawningIsAllowedWhileAllowSpawningIsTrue() {
        config.set("allowSpawning", true);

        assertTrue(noMoreCreepers.isSpawningAllowed());
    }

    @Test
    public void spawningIsBlockedWhileAllowSpawningIsFalse() {
        config.set("allowSpawning", false);

        assertFalse(noMoreCreepers.isSpawningAllowed());
    }

    /** With no {@code allowSpawning} line at all, creepers are blocked, not let through. */
    @Test
    public void spawningIsBlockedWhenAllowSpawningIsMissing() {
        assertFalse(noMoreCreepers.isSpawningAllowed());
    }

    @Test
    public void spawningIsBlockedForANonBooleanAllowSpawningValue() {
        config.set("allowSpawning", "yes");

        assertFalse(noMoreCreepers.isSpawningAllowed());
    }

    @Test
    public void bareCommandShowsPluginInformationWithoutTheCommandService() {
        assertTrue(noMoreCreepers.onCommand(sender, command, "nmc", new String[0]));

        verify(sender).sendMessage(ChatColor.AQUA + "NoMoreCreepers v1.3.2");
        verifyNoInteractions(commandService);
    }

    @Test
    public void subCommandIsHandedToTheCommandServiceWithItsRegisteredNameAndArguments() {
        String[] args = {"config", "show"};
        when(commandService.interpretAndExecuteCommand(sender, "nmc", args)).thenReturn(true);

        assertTrue(noMoreCreepers.onCommand(sender, command, "nomorecreepers:nmc", args));

        verify(commandService).interpretAndExecuteCommand(sender, "nmc", args);
        verifyNoInteractions(sender);
    }

    @Test
    public void commandServiceResultIsReturnedAsIs() {
        String[] args = {"unknown"};
        when(commandService.interpretAndExecuteCommand(sender, "nmc", args)).thenReturn(false);

        assertFalse(noMoreCreepers.onCommand(sender, command, "nmc", args));
    }

    /**
     * The usage event names the registered command, not the label it was typed as, and
     * carries nothing typed after it: the README promises the arguments are never sent.
     */
    @Test
    public void subCommandReportsACommandEventCarryingOnlyTheCommandName() throws Exception {
        when(command.getName()).thenReturn("nomorecreepers");
        String[] args = {"config", "set", "allowSpawning", "true"};

        String body = reportedBody(() -> noMoreCreepers.onCommand(sender, command, "nmc", args));

        assertEquals("{\"application\":\"NoMoreCreepers\",\"name\":\"command\","
                + "\"tags\":{\"name\":\"nomorecreepers\",\"version\":\"1.3.2\"}}", body);
    }

    @Test
    public void bareCommandAlsoReportsACommandEvent() throws Exception {
        String body = reportedBody(() -> noMoreCreepers.onCommand(sender, command, "nmc", new String[0]));

        assertEquals("{\"application\":\"NoMoreCreepers\",\"name\":\"command\","
                + "\"tags\":{\"name\":\"nmc\",\"version\":\"1.3.2\"}}", body);
    }

    /**
     * Only a command a player runs is counted. Each case runs the non-player command first and
     * a player's command after it on the same client, so the one event that arrives shows the
     * client was sending, and that it is the player's.
     */
    @Test
    public void consoleCommandReportsNoUsageEvent() throws Exception {
        assertOnlyThePlayersCommandIsReported(mock(ConsoleCommandSender.class));
    }

    @Test
    public void commandBlockCommandReportsNoUsageEvent() throws Exception {
        assertOnlyThePlayersCommandIsReported(mock(BlockCommandSender.class));
    }

    @Test
    public void nonPlayerCommandIsStillRouted() {
        CommandSender console = mock(ConsoleCommandSender.class);
        String[] args = {"config", "show"};
        when(commandService.interpretAndExecuteCommand(console, "nmc", args)).thenReturn(true);

        assertTrue(noMoreCreepers.onCommand(console, command, "nmc", args));

        verify(commandService).interpretAndExecuteCommand(console, "nmc", args);
    }

    /** The notice is the operator's disclosure, so it names where events really go. */
    @Test
    public void startupNoticeNamesTheConfiguredEndpoint() throws Exception {
        config.set("usage-reporting.enabled", true);
        config.set("usage-reporting.endpoint", "http://127.0.0.1:1");
        config.set("usage-reporting.key", "test-key");

        initializeUsageReporting();

        ArgumentCaptor<String> notice = ArgumentCaptor.forClass(String.class);
        verify(logger).info(notice.capture());
        assertTrue(notice.getValue().startsWith("Usage reporting is on: NoMoreCreepers sends its name, version "
                + "and command names to http://127.0.0.1:1,"), notice.getValue());
    }

    @Test
    public void startupNoticeSaysWhyReportingIsOff() throws Exception {
        config.set("usage-reporting.enabled", false);
        config.set("usage-reporting.endpoint", "http://127.0.0.1:1");
        config.set("usage-reporting.key", "test-key");

        initializeUsageReporting();

        verify(logger).info("Usage reporting is off (config.yml).");
    }

    /** Thrown out of onEnable, the client's refusal of a blank URL would skip the notice. */
    @Test
    public void blankEndpointLeavesReportingOffInsteadOfFailingStartup() throws Exception {
        config.set("usage-reporting.enabled", true);
        config.set("usage-reporting.endpoint", "");
        config.set("usage-reporting.key", "test-key");

        initializeUsageReporting();

        verify(logger).info("Usage reporting is off (config.yml).");
    }

    /**
     * Runs the startup step that onEnable ends with, on the mock. getDescription and
     * getDataFolder are final, so they run for real and read these fields. The environment is
     * emptied as in {@link #reportedBody}, and the client is closed so no thread outlives the test.
     */
    private void initializeUsageReporting() throws Exception {
        setPluginField("description", new PluginDescriptionFile("NoMoreCreepers", "1.3.2", "main"));
        setPluginField("dataFolder", new File(pluginsDirectory, "NoMoreCreepers"));
        when(noMoreCreepers.getLogger()).thenReturn(logger);
        Method initialize = NoMoreCreepers.class.getDeclaredMethod("initializeUsageReporting");
        initialize.setAccessible(true);

        Field environmentField = TraceClient.class.getDeclaredField("environment");
        environmentField.setAccessible(true);
        Object realEnvironment = environmentField.get(null);
        Function<String, String> emptyEnvironment = name -> null;
        environmentField.set(null, emptyEnvironment);
        try {
            initialize.invoke(noMoreCreepers);
        } finally {
            environmentField.set(null, realEnvironment);
            closeTrace();
        }
    }

    private void setPluginField(String name, Object value) throws ReflectiveOperationException {
        Field field = JavaPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(noMoreCreepers, value);
    }

    private void assertOnlyThePlayersCommandIsReported(CommandSender nonPlayer) throws Exception {
        when(command.getName()).thenReturn("nomorecreepers");
        Command playerCommand = mock(Command.class);
        when(playerCommand.getName()).thenReturn("nmc");

        String body = reportedBody(() -> {
            noMoreCreepers.onCommand(nonPlayer, command, "nmc", new String[]{"config", "show"});
            noMoreCreepers.onCommand(sender, playerCommand, "nmc", new String[]{"help"});
        });

        assertEquals("{\"application\":\"NoMoreCreepers\",\"name\":\"command\","
                + "\"tags\":{\"name\":\"nmc\",\"version\":\"1.3.2\"}}", body);
    }

    /**
     * A count still open when the plugin is disabled is sent then (CONFIG.md, Usage reporting).
     * The trace client only does that if onDisable closes it; the bodies are read straight
     * after onDisable, before the test's own close, so only the plugin's close can send it.
     */
    @Test
    public void disablingSendsTheCountOfARepeatedCommandStillBeingCoalesced() throws Exception {
        doCallRealMethod().when(noMoreCreepers).onDisable();
        String[] args = {"help"};

        List<String> bodies = bodiesSentDuring(() -> {
            noMoreCreepers.onCommand(sender, command, "nmc", args);
            noMoreCreepers.onCommand(sender, command, "nmc", args);
            noMoreCreepers.onCommand(sender, command, "nmc", args);
            noMoreCreepers.onDisable();
        });

        assertEquals(2, bodies.size(), bodies.toString());
        assertEquals("{\"application\":\"NoMoreCreepers\",\"name\":\"command\","
                + "\"tags\":{\"name\":\"nmc\",\"version\":\"1.3.2\"}}", bodies.get(0));
        assertEquals("{\"application\":\"NoMoreCreepers\",\"name\":\"command\",\"value\":2.0,"
                + "\"tags\":{\"name\":\"nmc\",\"version\":\"1.3.2\",\"coalesced\":\"2\"}}", bodies.get(1));
    }

    /**
     * Runs {@code action} with an enabled trace client and returns the body of the one event it
     * sent. The client is closed before the count is checked, and {@code close()} waits for
     * queued sends, so an extra event would be counted.
     */
    private String reportedBody(Runnable action) throws Exception {
        List<String> bodies = bodiesSentDuring(() -> {
            action.run();
            closeTrace();
        });
        assertEquals(1, bodies.size(), bodies.toString());
        return bodies.get(0);
    }

    private void closeTrace() {
        try {
            Field traceField = NoMoreCreepers.class.getDeclaredField("trace");
            traceField.setAccessible(true);
            ((TraceClient) traceField.get(noMoreCreepers)).close();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Runs {@code action} with an enabled trace client pointed at an HTTP server on a loopback
     * port, and returns the bodies the server had received when {@code action} returned. The
     * client's environment is emptied for the duration, so a DO_NOT_TRACK on the machine running
     * the suite cannot disable it. The client is closed only afterwards, so anything it would
     * send on closing is not in the list unless {@code action} closed it.
     */
    private List<String> bodiesSentDuring(Runnable action) throws Exception {
        List<String> bodies = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            InputStream in = exchange.getRequestBody();
            byte[] buffer = new byte[1024];
            for (int read; (read = in.read(buffer)) != -1; ) {
                body.write(buffer, 0, read);
            }
            bodies.add(new String(body.toByteArray(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        server.start();

        Field environmentField = TraceClient.class.getDeclaredField("environment");
        environmentField.setAccessible(true);
        Object realEnvironment = environmentField.get(null);
        Function<String, String> emptyEnvironment = name -> null;
        environmentField.set(null, emptyEnvironment);
        TraceClient trace = null;
        try {
            trace = TraceClient.builder("http://127.0.0.1:" + server.getAddress().getPort(), "NoMoreCreepers", "1.3.2")
                    .key("test-key")
                    .build();
            assertTrue(trace.isEnabled());
            setField("trace", trace);

            action.run();

            return new ArrayList<>(bodies);
        } finally {
            if (trace != null) {
                trace.close();
            }
            environmentField.set(null, realEnvironment);
            server.stop(0);
        }
    }
}
