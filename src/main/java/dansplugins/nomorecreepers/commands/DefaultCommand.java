package dansplugins.nomorecreepers.commands;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

public class DefaultCommand extends AbstractPluginCommand {
    private final NoMoreCreepers noMoreCreepers;

    public DefaultCommand(NoMoreCreepers noMoreCreepers) {
        super(new ArrayList<>(Arrays.asList("default")), new ArrayList<>(Arrays.asList("nmc.default")));
        this.noMoreCreepers = noMoreCreepers;
    }

    @Override
    public boolean execute(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.AQUA + "NoMoreCreepers " + noMoreCreepers.getVersion());
        commandSender.sendMessage(ChatColor.AQUA + "Developed by: Daniel Stephenson");
        commandSender.sendMessage(ChatColor.AQUA + "Wiki: https://github.com/dmccoystephenson/NoMoreCreepers/wiki");
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }
}