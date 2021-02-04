package ru.idleness.minecraft.chatdisabler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public final class ChatDisabler extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        flushConfig();

        this.getServer().getPluginManager().registerEvents(new Chat(), this);

        this.getCommand("chatdisabler").setExecutor(new ToggleChat());
    }

    public void flushConfig() {
        config = config != null ? config : this.getConfig();

        config.addDefault("state", false);

        config.addDefault("hasBeenDisabled", "Chat has been §a§ldisabled.");

        config.addDefault("hasBeenEnabled", "Chat has been §a§lenabled.");

        config.addDefault("disabled", "Chat is §a§ldisabled.");

        config.options().copyDefaults(true);
        this.saveConfig();
    }

    private class ToggleChat implements CommandExecutor, TabCompleter {

        public void toggle(CommandSender sender, boolean newState) {
            if (newState == config.getBoolean("state"))
                return;

            boolean hasPermission = true;
            if (sender instanceof Player)
                hasPermission = sender.hasPermission("chatdisabler.toggle");

            if (!hasPermission)
                return;

            config.set("state", newState);
            flushConfig();

            String msg = config.getString(config.getBoolean("state") ? "hasBeenEnabled" : "hasBeenDisabled");

            if (msg.length() != 0)
                sender.sendMessage(msg);
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return Arrays.asList("", "on", "off");
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            boolean newState = !config.getBoolean("state");

            if (args.length > 0)
                newState = args[0].toLowerCase().equals("on");

            toggle(sender, newState);

            return true;
        }
    }

    private class Chat implements Listener {

        @EventHandler
        public void onChat(AsyncPlayerChatEvent e) {

            if (config.getBoolean("state") || e.getPlayer().hasPermission("chatdisabler.bypass"))
                return;

            e.setCancelled(true);

            String msg = config.getString("disabled");
            if (msg.length() > 0)
                e.getPlayer().sendMessage(msg);
        }
    }
}
