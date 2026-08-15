package dansplugins.nomorecreepers.commands;

import dansplugins.nomorecreepers.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Characterizes sub-command dispatch for {@code /nmc config}.
 */
public class ConfigCommandTest {
    private static final String SUB_COMMAND_MESSAGE = ChatColor.RED + "Sub-commands: show, set";
    private static final String USAGE_MESSAGE = ChatColor.RED + "Usage: /nmc config set (option) (value)";

    private final ConfigService configService = mock(ConfigService.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final ConfigCommand configCommand = new ConfigCommand(configService);

    @Test
    public void bareCommandListsSubCommands() {
        assertFalse(configCommand.execute(sender));

        verify(sender).sendMessage(SUB_COMMAND_MESSAGE);
        verifyNoInteractions(configService);
    }

    @Test
    public void emptyArgumentsListSubCommands() {
        assertFalse(configCommand.execute(sender, new String[]{}));

        verify(sender).sendMessage(SUB_COMMAND_MESSAGE);
        verifyNoInteractions(configService);
    }

    @Test
    public void unknownSubCommandListsSubCommands() {
        assertFalse(configCommand.execute(sender, new String[]{"reload"}));

        verify(sender).sendMessage(SUB_COMMAND_MESSAGE);
        verifyNoInteractions(configService);
    }

    @Test
    public void showSendsTheConfigList() {
        assertTrue(configCommand.execute(sender, new String[]{"show"}));

        verify(configService).sendConfigList(sender);
    }

    @Test
    public void subCommandsAreMatchedCaseInsensitively() {
        assertTrue(configCommand.execute(sender, new String[]{"SHOW"}));

        verify(configService).sendConfigList(sender);
    }

    @Test
    public void setWithTooFewArgumentsShowsUsage() {
        assertFalse(configCommand.execute(sender, new String[]{"set", "allowSpawning"}));

        verify(sender).sendMessage(USAGE_MESSAGE);
        verify(configService, never()).setConfigOption(anyString(), anyString(), any());
    }

    @Test
    public void setForwardsTheOptionAndValueToTheConfigService() {
        assertTrue(configCommand.execute(sender, new String[]{"set", "allowSpawning", "true"}));

        verify(configService).setConfigOption("allowSpawning", "true", sender);
    }

    @Test
    public void setIgnoresArgumentsBeyondTheValue() {
        assertTrue(configCommand.execute(sender, new String[]{"set", "allowSpawning", "true", "extra"}));

        verify(configService).setConfigOption("allowSpawning", "true", sender);
    }
}
