package dansplugins.nomorecreepers;

import com.sun.net.httpserver.HttpServer;
import dansplugins.nomorecreepers.services.ConfigService;
import dansplugins.nomorecreepers.trace.TraceClient;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import preponderous.ponder.minecraft.bukkit.services.CommandService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Characterizes the plugin class's own logic: the version check that decides, on startup,
 * whether an existing config.yml gets the missing-defaults pass; the {@code allowSpawning}
 * lookup the spawn listener relies on; and how {@code onCommand} routes a command and what
 * usage event it reports for it. A false negative in the version check leaves a new option
 * off disk after an upgrade; a false positive rewrites the version on every start.
 */
public class NoMoreCreepersTest {
    private final NoMoreCreepers noMoreCreepers = mock(NoMoreCreepers.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final Command command = mock(Command.class);
    private final CommandService commandService = mock(CommandService.class);
    private FileConfiguration config;

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
    public void subCommandIsHandedToTheCommandServiceWithItsLabelAndArguments() {
        String[] args = {"config", "show"};
        when(commandService.interpretAndExecuteCommand(sender, "nomorecreepers", args)).thenReturn(true);

        assertTrue(noMoreCreepers.onCommand(sender, command, "nomorecreepers", args));

        verify(commandService).interpretAndExecuteCommand(sender, "nomorecreepers", args);
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
     * Runs {@code action} with an enabled trace client pointed at an HTTP server on a loopback
     * port, and returns the body of the one event it sent. The client's environment is emptied
     * for the duration, so a DO_NOT_TRACK on the machine running the suite cannot disable it.
     */
    private String reportedBody(Runnable action) throws Exception {
        List<String> bodies = new CopyOnWriteArrayList<>();
        CountDownLatch arrived = new CountDownLatch(1);
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
            arrived.countDown();
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

            assertTrue(arrived.await(5, TimeUnit.SECONDS), "the command event should reach the server");
            trace.close();
            assertEquals(1, bodies.size(), bodies.toString());
            return bodies.get(0);
        } finally {
            if (trace != null) {
                trace.close();
            }
            environmentField.set(null, realEnvironment);
            server.stop(0);
        }
    }
}
