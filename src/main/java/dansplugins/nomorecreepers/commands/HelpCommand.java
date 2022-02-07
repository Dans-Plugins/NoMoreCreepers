package dansplugins.nomorecreepers.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import java.util.ArrayList;
import java.util.Arrays;

public class HelpCommand extends AbstractPluginCommand {

    public HelpCommand() {
        super(new ArrayList<>(Arrays.asList("help")), new ArrayList<>(Arrays.asList("nmc.help")));
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "/nmc - View plugin information.");
        sender.sendMessage(ChatColor.AQUA + "/nmc help - View a list of helpful commands.");
        sender.sendMessage(ChatColor.AQUA + "/nmc config - View and set config options.");
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }

}