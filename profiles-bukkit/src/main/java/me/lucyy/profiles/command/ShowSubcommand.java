package me.lucyy.profiles.command;

import me.lucyy.common.command.CommandHelper;
import me.lucyy.common.command.Subcommand;
import me.lucyy.common.format.TextFormatter;
import me.lucyy.profiles.api.SettableProfileField;
import me.lucyy.profiles.config.ConfigHandler;
import me.lucyy.profiles.FormatInverter;
import me.lucyy.profiles.ProFiles;
import me.lucyy.profiles.api.ProfileField;
import me.lucyy.profiles.field.SimpleProfileField;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShowSubcommand implements Subcommand {

    private final ProFiles plugin;

    public ShowSubcommand(ProFiles plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Shows your, or another player's, profile.";
    }

    @Override
    public String getUsage() {
        return "set";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, CommandSender ignored, String[] args) {
        ConfigHandler cfg = plugin.getConfigHandler();
        OfflinePlayer target;
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(cfg.getPrefix() + "Please specify a username.");
            return true;
        }

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
        } else target = (Player) sender;

        if (target == null) {
            sender.sendMessage(cfg.getPrefix() + cfg.formatMain("Player '")
                    + cfg.formatAccent(args[0])
                    + cfg.formatMain("' could not be found."));
            return true;
        }

        StringBuilder output = new StringBuilder().append("\n")
                .append(TextFormatter.formatTitle(target.getName() + "'s profile", cfg))
                .append("\n");

        if (plugin.getConfigHandler().subtitleEnabled()) {
            String subtitle = plugin.getProfileManager().getField("subtitle").getValue(target.getUniqueId());
            if (!subtitle.equals("Unset")) {
                output.append(TextFormatter.centreText(subtitle, new FormatInverter(plugin.getConfigHandler()), " "))
                        .append("\n");
            }
        }

        plugin.getProfileManager().getFields().stream()
                .sorted(Comparator.comparingInt(ProfileField::getOrder)).forEach(field -> {
            if (!field.getKey().equals("subtitle")) {
            	String value = field.getValue(target.getUniqueId());
                output.append(cfg.formatMain(field.getDisplayName() + ": "))
                        .append(CommandUtils.formatIfNotAlready(value, cfg))
                        .append("\n");
			}
        });
        output.append(TextFormatter.formatTitle("*", cfg)).append("\n");
        sender.sendMessage(output.toString());
        return true;
    }

    @Override
    public List<String> tabComplete(String[] args) {
        if (args.length != 2) return new ArrayList<>();
        return CommandHelper.tabCompleteNames(args[1]);
    }
}
