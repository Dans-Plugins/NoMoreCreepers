package dansplugins.nomorecreepers.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Pins the help listing, which has to stay in step with COMMANDS.md.
 */
public class HelpCommandTest {
    private final CommandSender sender = mock(CommandSender.class);
    private final HelpCommand helpCommand = new HelpCommand();

    @Test
    public void everyCommandIsListed() {
        assertTrue(helpCommand.execute(sender));

        verify(sender).sendMessage(ChatColor.AQUA + "/nmc - View plugin information.");
        verify(sender).sendMessage(ChatColor.AQUA + "/nmc help - View a list of helpful commands.");
        verify(sender).sendMessage(ChatColor.AQUA + "/nmc config - View and set config options.");
    }

    @Test
    public void argumentsAreIgnored() {
        assertTrue(helpCommand.execute(sender, new String[]{"anything"}));

        verify(sender).sendMessage(ChatColor.AQUA + "/nmc - View plugin information.");
    }
}
