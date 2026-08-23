package dansplugins.nomorecreepers.commands;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the plugin information shown by a bare {@code /nmc}.
 */
public class DefaultCommandTest {
    private final NoMoreCreepers noMoreCreepers = mock(NoMoreCreepers.class);
    private final CommandSender sender = mock(CommandSender.class);

    @Test
    public void pluginInformationIncludesTheRunningVersion() {
        when(noMoreCreepers.getVersion()).thenReturn("v2.0.0");

        assertTrue(new DefaultCommand(noMoreCreepers).execute(sender));

        verify(sender).sendMessage(ChatColor.AQUA + "NoMoreCreepers v2.0.0");
        verify(sender).sendMessage(ChatColor.AQUA + "Developed by: Daniel Stephenson");
        verify(sender).sendMessage(ChatColor.AQUA
                + "Wiki: https://github.com/Dans-Plugins/NoMoreCreepers/wiki");
    }

    @Test
    public void noPermissionNodeIsDeclared() {
        assertTrue(new DefaultCommand(noMoreCreepers).getPermissions().isEmpty());
    }

    @Test
    public void argumentsAreIgnored() {
        when(noMoreCreepers.getVersion()).thenReturn("v2.0.0");

        assertTrue(new DefaultCommand(noMoreCreepers).execute(sender, new String[]{"anything"}));

        verify(sender).sendMessage(ChatColor.AQUA + "NoMoreCreepers v2.0.0");
    }
}
