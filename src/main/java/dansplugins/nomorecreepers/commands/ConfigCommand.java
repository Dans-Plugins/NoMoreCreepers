package dansplugins.nomorecreepers.commands;

import dansplugins.nomorecreepers.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

public class ConfigCommand extends AbstractPluginCommand {
    private final ConfigService configService;

    public ConfigCommand(ConfigService configService) {
        super(new ArrayList<>(Arrays.asList("config")), new ArrayList<>(Arrays.asList("nmc.config")));
        this.configService = configService;
    }

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