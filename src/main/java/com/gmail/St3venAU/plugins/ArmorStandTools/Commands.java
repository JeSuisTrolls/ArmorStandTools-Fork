package com.gmail.St3venAU.plugins.ArmorStandTools;

import org.jetbrains.annotations.NotNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Commands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            AST.plugin.getLogger().warning(Config.notConsole);
            return false;
        }
        String cmd = command.getName().toLowerCase();
        if (cmd.equals("astools") || cmd.equals("ast")) {
            if (Config.useCommandForTextInput && args.length > 0) {
                StringBuilder sb = new StringBuilder();
                boolean space = false;
                for (String s : args) {
                    if (space) sb.append(' ');
                    space = true;
                    sb.append(s);
                }
                if (AST.processInput(p, sb.toString())) return true;
            }
            if (!Utils.hasPermissionNode(p, "astools.command")) {
                p.sendMessage(MM.parse(Config.noCommandPerm));
                return true;
            }
            if (args.length == 0) {
                UUID uuid = p.getUniqueId();
                if (AST.savedInventories.containsKey(uuid)) {
                    AST.restoreInventory(p);
                } else {
                    AST.plugin.saveInventoryAndClear(p);
                    ArmorStandTool.give(p);
                    p.sendMessage(MM.parse(Config.giveMsg1));
                    p.sendMessage(MM.parse(Config.giveMsg2));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (Utils.hasPermissionNode(p, "astools.reload")) {
                    Config.reload();
                    p.sendMessage(MM.parse(Config.conReload));
                } else {
                    p.sendMessage(MM.parse(Config.noRelPerm));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        String cmd = command.getName().toLowerCase();
        String typed = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        return list;
    }
}