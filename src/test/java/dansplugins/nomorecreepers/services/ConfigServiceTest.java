package dansplugins.nomorecreepers.services;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
