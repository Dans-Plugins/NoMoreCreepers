package dansplugins.nomorecreepers.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import preponderous.ponder.misc.AbstractCommand;

import java.util.ArrayList;
import java.util.Collections;

public class HelpCommand extends AbstractCommand {

    private ArrayList<String> names = new ArrayList<>(Collections.singletonList("help"));
    private ArrayList<String> permissions = new ArrayList<>(Collections.singletonList("nmc.help"));

    @Override
    public ArrayList<String> getNames() {
        return names;
    }

    @Override
    public ArrayList<String> getPermissions() {
        return permissions;
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