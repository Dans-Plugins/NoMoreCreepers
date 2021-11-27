package dansplugins.nomorecreepers.commands;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import preponderous.ponder.misc.AbstractCommand;
import preponderous.ponder.services.ConfigService;

import java.util.ArrayList;
import java.util.Collections;

public class ConfigCommand extends AbstractCommand {
    private ArrayList<String> names = new ArrayList<>(Collections.singletonList("config"));
    private ArrayList<String> permissions = new ArrayList<>(Collections.singletonList("nmc.config"));

    @Override
    public ArrayList<String> getNames() {
        return names;
    }

    @Override
    public ArrayList<String> getPermissions() {
        return permissions;
    }

    ConfigService configService = NoMoreCreepers.getInstance().getPonderAPI().getConfigService();

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Sub-commands: show, set");
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Sub-commands: show, set");
            return false;
        }

        if (args[0].equalsIgnoreCase("show")) {
            configService.sendConfigList(sender);
            return true;
        }
        else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /c config set (option) (value)");
                return false;
            }
            String option = args[1];

            String value = args[2];

            configService.setConfigOption(option, value, sender);
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Sub-commands: show, set");
            return false;
        }
    }

}