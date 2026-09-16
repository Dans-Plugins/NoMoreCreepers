package dansplugins.nomorecreepers.services;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Characterizes the configuration behaviour that is reachable without a running server.
 */
public class ConfigServiceTest {
    private final NoMoreCreepers noMoreCreepers = mock(NoMoreCreepers.class);
    private final CommandSender sender = mock(CommandSender.class);
    private FileConfiguration config;
    private ConfigService configService;

    @BeforeEach
    public void setUp() {
        config = new YamlConfiguration();
        when(noMoreCreepers.getConfig()).thenReturn(config);
        when(noMoreCreepers.getVersion()).thenReturn("v2.0.0");
        configService = new ConfigService(noMoreCreepers);
    }

    @Test
    public void missingDefaultsAreWritten() {
        configService.saveMissingConfigDefaultsIfNotPresent();

        assertEquals("v2.0.0", config.getString("version"));
        assertFalse(config.getBoolean("debugMode"));
        assertFalse(config.getBoolean("allowSpawning"));
        verify(noMoreCreepers).saveConfig();
    }

    @Test
    public void existingValuesSurviveTheDefaultsPass() {
        config.set("allowSpawning", true);

        configService.saveMissingConfigDefaultsIfNotPresent();

        assertTrue(config.getBoolean("allowSpawning"));
    }

    @Test
    public void versionIsOverwrittenByTheDefaultsPass() {
        config.set("version", "v1.0.0");

        configService.saveMissingConfigDefaultsIfNotPresent();

        assertEquals("v2.0.0", config.getString("version"));
    }

    @Test
    public void booleanOptionIsSetAndTheConfigIsSaved() {
        config.set("allowSpawning", false);

        configService.setConfigOption("allowSpawning", "true", sender);

        assertTrue(config.getBoolean("allowSpawning"));
        assertTrue(configService.hasBeenAltered());
        verify(sender).sendMessage(ChatColor.GREEN + "Boolean set.");
        verify(noMoreCreepers).saveConfig();
    }

    /**
     * The option name is looked up with a case-sensitive {@code isSet} check before the
     * case-insensitive branch comparisons are reached, so a mis-cased name is reported as
     * unknown. This pins the current behaviour rather than endorsing it.
     */
    @Test
    public void optionNameLookupIsCaseSensitive() {
        config.set("allowSpawning", false);

        configService.setConfigOption("ALLOWSPAWNING", "true", sender);

        assertFalse(config.isSet("ALLOWSPAWNING"));
        assertFalse(config.getBoolean("allowSpawning"));
        assertFalse(configService.hasBeenAltered());
        verify(sender).sendMessage(ChatColor.RED + "That config option wasn't found.");
    }

    @Test
    public void unparseableBooleanValueBecomesFalse() {
        config.set("allowSpawning", true);

        configService.setConfigOption("allowSpawning", "yes", sender);

        assertFalse(config.getBoolean("allowSpawning"));
        verify(sender).sendMessage(ChatColor.GREEN + "Boolean set.");
    }

    @Test
    public void versionCannotBeSet() {
        config.set("version", "v2.0.0");

        configService.setConfigOption("version", "v9.9.9", sender);

        assertEquals("v2.0.0", config.getString("version"));
        assertFalse(configService.hasBeenAltered());
        verify(sender).sendMessage(ChatColor.RED + "Cannot set version.");
        verify(noMoreCreepers, never()).saveConfig();
    }

    @Test
    public void unknownOptionIsRejected() {
        configService.setConfigOption("thisIsNotAnOption", "true", sender);

        assertFalse(configService.hasBeenAltered());
        verify(sender).sendMessage(ChatColor.RED + "That config option wasn't found.");
        verify(noMoreCreepers, never()).saveConfig();
    }

    @Test
    public void anExistingNonBooleanOptionIsStoredAsAString() {
        config.set("someOtherOption", "old");

        configService.setConfigOption("someOtherOption", "new", sender);

        assertEquals("new", config.getString("someOtherOption"));
        verify(sender).sendMessage(ChatColor.GREEN + "String set.");
    }

    @Test
    public void configListNamesEveryStoredOption() {
        config.set("version", "v2.0.0");
        config.set("debugMode", false);
        config.set("allowSpawning", true);

        configService.sendConfigList(sender);

        verify(sender).sendMessage(ChatColor.AQUA + "=== Config List ===");
        verify(sender).sendMessage(ChatColor.AQUA
                + "version: v2.0.0, debugMode: false, allowSpawning: true");
    }

    @Test
    public void usageReportingBlockIsCopiedFromTheBundledDefaultsWhenTheFileHasNone() {
        // An installation from before usage reporting: version and options on disk, no block.
        config.set("version", "v2.0.0");
        config.set("debugMode", false);
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.endpoint", "https://trace.danielstephenson.dev");
        bundled.set("usage-reporting.key", "bundled-key");
        config.setDefaults(bundled);

        configService.saveUsageReportingDefaultsIfNotPresent();

        assertTrue(config.isSet("usage-reporting.enabled"));
        assertEquals("https://trace.danielstephenson.dev", config.get("usage-reporting.endpoint"));
        assertEquals("bundled-key", config.get("usage-reporting.key"));
        assertEquals("v2.0.0", config.getString("version"));
        verify(noMoreCreepers).saveConfig();
    }

    @Test
    public void anExistingUsageReportingBlockIsLeftAloneAndNotResaved() {
        config.set("usage-reporting.enabled", false);
        config.set("usage-reporting.endpoint", "http://localhost:8080");
        config.set("usage-reporting.key", "operator-key");
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.key", "bundled-key");
        config.setDefaults(bundled);

        configService.saveUsageReportingDefaultsIfNotPresent();

        assertFalse(config.getBoolean("usage-reporting.enabled"));
        assertEquals("operator-key", config.getString("usage-reporting.key"));
        verify(noMoreCreepers, never()).saveConfig();
    }

    @Test
    public void usageReportingBlockIsNotInventedWithoutBundledDefaults() {
        configService.saveUsageReportingDefaultsIfNotPresent();

        assertFalse(config.isSet("usage-reporting"));
        verify(noMoreCreepers, never()).saveConfig();
    }

    @Test
    public void serviceStartsUnaltered() {
        assertFalse(configService.hasBeenAltered());
    }

    @Test
    public void configuredIntIsReturnedByGetIntOrDefault() {
        config.set("someInt", 5);

        assertEquals(5, configService.getIntOrDefault("someInt", 7));
    }

    @Test
    public void unsetIntFallsBackToTheDefault() {
        assertEquals(7, configService.getIntOrDefault("someInt", 7));
    }

    /**
     * A configured zero is indistinguishable from "unset" here, so the default wins.
     * This pins the current behaviour rather than endorsing it.
     */
    @Test
    public void configuredZeroIntStillFallsBackToTheDefault() {
        config.set("someInt", 0);

        assertEquals(7, configService.getIntOrDefault("someInt", 7));
    }

    @Test
    public void configuredDoubleIsReturnedByGetDoubleOrDefault() {
        config.set("someDouble", 1.5);

        assertEquals(1.5, configService.getDoubleOrDefault("someDouble", 2.5));
    }

    @Test
    public void configuredZeroDoubleStillFallsBackToTheDefault() {
        config.set("someDouble", 0.0);

        assertEquals(2.5, configService.getDoubleOrDefault("someDouble", 2.5));
    }

    @Test
    public void configuredStringIsReturnedByGetStringOrDefault() {
        config.set("someString", "configured");

        assertEquals("configured", configService.getStringOrDefault("someString", "fallback"));
    }

    @Test
    public void unsetStringFallsBackToTheDefault() {
        assertEquals("fallback", configService.getStringOrDefault("someString", "fallback"));
    }

    /**
     * Unlike the numeric variants, an empty string is a value and is returned as one.
     */
    @Test
    public void configuredEmptyStringIsReturnedAsIs() {
        config.set("someString", "");

        assertEquals("", configService.getStringOrDefault("someString", "fallback"));
    }

    /**
     * A server upgraded from before usage reporting has no usage-reporting block in its
     * config.yml, and the file is never rewritten. Bukkit registers the jar's config.yml as
     * the defaults for that file, and the one-argument getters fall through to them, so the
     * bundled key must be what such a server reads.
     */
    @Test
    public void usageReportingReadsThroughToTheBundledDefaultsWhenTheFileHasNoBlock() {
        config.setDefaults(bundledConfig());

        assertFalse(config.isSet("usage-reporting.key"), "the on-disk file must not carry the block for this test to mean anything");
        assertTrue(configService.isUsageReportingEnabled());
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertEquals("mA-2vVuMd88tSXEtI5hdXomxJK5nQ6-RThSgb8aNfUw", configService.getUsageReportingKey());
    }

    /**
     * The two-argument getters return their explicit fallback instead of falling through to
     * the bundled defaults, which for the key would turn reporting off on every existing
     * installation. This pins the one-argument calls.
     */
    @Test
    public void usageReportingUsesTheOneArgumentGetters() {
        FileConfiguration mocked = mock(FileConfiguration.class);
        when(noMoreCreepers.getConfig()).thenReturn(mocked);
        when(mocked.getBoolean("usage-reporting.enabled")).thenReturn(true);
        when(mocked.getString("usage-reporting.endpoint")).thenReturn("https://trace.danielstephenson.dev");
        when(mocked.getString("usage-reporting.key")).thenReturn("bundled-key");

        assertTrue(configService.isUsageReportingEnabled());
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertEquals("bundled-key", configService.getUsageReportingKey());
        verify(mocked, never()).getString(eq("usage-reporting.key"), anyString());
        verify(mocked, never()).getString(eq("usage-reporting.endpoint"), anyString());
        verify(mocked, never()).getBoolean(eq("usage-reporting.enabled"), anyBoolean());
    }

    @Test
    public void usageReportingIsOffWithNoKeyAnywhere() {
        assertEquals("", configService.getUsageReportingKey(), "no key anywhere must read as off, not as null");
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
        assertFalse(configService.isUsageReportingEnabled());
    }

    @Test
    public void usageReportingReadsTheConfiguredValues() {
        config.setDefaults(bundledConfig());
        config.set("usage-reporting.enabled", false);
        config.set("usage-reporting.endpoint", "http://localhost:8080");
        config.set("usage-reporting.key", "abc");

        assertFalse(configService.isUsageReportingEnabled());
        assertEquals("http://localhost:8080", configService.getUsageReportingEndpoint());
        assertEquals("abc", configService.getUsageReportingKey());
    }

    /**
     * On a fresh install, and on a version mismatch, the defaults pass saves with
     * copyDefaults on, which is what puts the bundled block into the file on disk.
     */
    @Test
    public void defaultsPassWritesTheBundledUsageReportingBlock() {
        config.setDefaults(bundledConfig());

        configService.saveMissingConfigDefaultsIfNotPresent();

        String saved = config.saveToString();
        assertTrue(saved.contains("usage-reporting:"), saved);
        assertTrue(saved.contains("key: mA-2vVuMd88tSXEtI5hdXomxJK5nQ6-RThSgb8aNfUw"), saved);
        assertTrue(saved.contains("endpoint: https://trace.danielstephenson.dev"), saved);
        assertTrue(saved.contains("enabled: true"), saved);
    }

    /** The config.yml shipped inside the jar, read from the classpath rather than from disk. */
    private static YamlConfiguration bundledConfig() {
        InputStream stream = ConfigServiceTest.class.getResourceAsStream("/config.yml");
        assertNotNull(stream, "config.yml must be bundled in the jar");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
