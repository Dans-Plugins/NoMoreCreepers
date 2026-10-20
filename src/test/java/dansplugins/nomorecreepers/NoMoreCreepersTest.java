package dansplugins.nomorecreepers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Characterizes the version check that decides, on startup, whether an existing config.yml
 * gets the missing-defaults pass. A false negative leaves a new option off disk after an
 * upgrade; a false positive rewrites the version on every start.
 */
public class NoMoreCreepersTest {
    private final NoMoreCreepers noMoreCreepers = mock(NoMoreCreepers.class);
    private FileConfiguration config;

    @BeforeEach
    public void setUp() {
        config = new YamlConfiguration();
        when(noMoreCreepers.getConfig()).thenReturn(config);
        when(noMoreCreepers.getVersion()).thenReturn("v1.3.2");
        when(noMoreCreepers.isVersionMismatched()).thenCallRealMethod();
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
}
