package com.gmail.St3venAU.plugins.ArmorStandTools;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
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
        } else if (cmd.equals("ascmd")) {
            ArmorStand as = getNearbyArmorStand(p);
            if (as == null) {
                p.sendMessage(MM.parse("\n" + Config.noASNearBy));
                return true;
            }
            // Nom de l'armorstand entre parenthèses si disponible
            Component asName;
            String rawName = as.getName();
            if (rawName.length() > 0 && !rawName.equalsIgnoreCase("armor stand")) {
                asName = MM.parse(" (<aqua>" + rawName + "</aqua>) ");
            } else {
                asName = MM.parse(" ");
            }
            String asNameStr = rawName.length() > 0 && !rawName.equalsIgnoreCase("armor stand")
                    ? " (<aqua>" + rawName + "</aqua>) " : " ";

            ArmorStandCmdManager asCmdManager = new ArmorStandCmdManager(as);
            if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
                if (!Utils.hasPermissionNode(p, "astools.ascmd.list")) {
                    p.sendMessage(MM.parse(Config.noCommandPerm));
                    return true;
                }
                listAssignedCommands(asCmdManager, asNameStr, p);
                return true;
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
                if (!Utils.hasPermissionNode(p, "astools.ascmd.remove")) {
                    p.sendMessage(MM.parse(Config.noCommandPerm));
                    return true;
                }
                int n;
                try {
                    n = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(MM.parse("<red>" + args[1] + " " + Config.isNotValidNumber));
                    return true;
                }
                if (asCmdManager.removeCommand(n - 1)) {
                    p.sendMessage(MM.parse("\n" + Config.command + " <green>#" + n + "</green> " + Config.removedFromAs + asNameStr));
                } else {
                    p.sendMessage(MM.parse("<red>" + args[1] + " " + Config.isNotValidNumber));
                }
                listAssignedCommands(asCmdManager, asNameStr, p);
                return true;
            } else if (args.length >= 5 && args[0].equalsIgnoreCase("add")) {
                CommandType type = CommandType.fromName(args[3]);
                if (type == null) { ascmdHelp(p); return true; }
                if (!Utils.hasPermissionNode(p, type.getAddPermission())) {
                    p.sendMessage(MM.parse(Config.noCommandPerm));
                    return true;
                }
                int priority;
                try { priority = Integer.parseInt(args[1]); }
                catch (NumberFormatException e) { p.sendMessage(MM.parse("<red>" + args[1] + " " + Config.isNotValidNumber)); return true; }
                int delay;
                try { delay = Integer.parseInt(args[2]); }
                catch (NumberFormatException e) { p.sendMessage(MM.parse("<red>" + args[2] + " " + Config.isNotValidNumber)); return true; }
                if (delay < 0) { p.sendMessage(MM.parse("<red>" + args[2] + " " + Config.isNotValidNumber)); return true; }
                StringBuilder sb = new StringBuilder();
                for (int i = 4; i < args.length; i++) sb.append(args[i]).append(" ");
                int startAt = sb.charAt(0) == '/' ? 1 : 0;
                String c = sb.substring(startAt, sb.length() - 1);
                if (c.isEmpty()) { ascmdHelp(p); return true; }
                asCmdManager.addCommand(new ArmorStandCmd(c, type, priority, delay), true);
                listAssignedCommands(asCmdManager, asNameStr, p);
                return true;
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("cooldown")) {
                if (!Utils.hasPermissionNode(p, "astools.ascmd.cooldown")) {
                    p.sendMessage(MM.parse(Config.noCommandPerm));
                    return true;
                }
                if (!asCmdManager.hasCommands()) {
                    p.sendMessage(MM.parse(Config.closestAS + asNameStr + Config.hasNoCmds));
                    return true;
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    asCmdManager.setCooldownTime(-1);
                    p.sendMessage(MM.parse(Config.cooldownRemovedFrom + " " + Config.closestAS + asNameStr));
                } else {
                    int ticks;
                    try { ticks = Integer.parseInt(args[1]); }
                    catch (NumberFormatException e) { p.sendMessage(MM.parse(args[1] + " " + Config.isAnInvalidCooldown)); return true; }
                    if (ticks < 0) { p.sendMessage(MM.parse(args[1] + " " + Config.isAnInvalidCooldown)); return true; }
                    asCmdManager.setCooldownTime(ticks);
                    p.sendMessage(MM.parse(Config.cooldownSetTo + " " + ticks + " " + Config.ticksFor + " " + Config.closestAS + asNameStr));
                }
                return true;
            } else {
                ascmdHelp(p);
                return true;
            }
        }
        return false;
    }

    private void listAssignedCommands(ArmorStandCmdManager asCmdManager, String name, Player p) {
        if (asCmdManager.hasCommands()) {
            p.sendMessage(MM.parse("\n" + Config.closestAS + name + Config.hasTheseCmdsAssigned));
            List<ArmorStandCmd> list = asCmdManager.getCommands();
            for (int n = 0; n < list.size(); n++) {
                ArmorStandCmd asCmd = list.get(n);
                p.sendMessage(MM.parse(
                        "<green>#" + (n + 1) + " " +
                                "<light_purple>" + Config.priority + ":</light_purple>" + asCmd.priority() + " " +
                                "<yellow>" + Config.delay + ":</yellow>" + asCmd.delay() + " " +
                                "<gold>" + Config.type + ":</gold>" + asCmd.type().getName() + " " +
                                "<aqua>" + Config.command + ":</aqua>" + asCmd.command()
                ));
            }
        } else {
            p.sendMessage(MM.parse("\n" + Config.closestAS + name + Config.hasNoCmds));
        }
    }

    private void ascmdHelp(Player p) {
        p.sendMessage(MM.parse("\n<aqua>" + Config.cmdHelp));
        p.sendMessage(MM.parse(Config.listAssignedCmds + ": <yellow>/ascmd list"));
        p.sendMessage(MM.parse(Config.addACmd + ": <yellow>/ascmd add <priority> <delay> <player/console> <command>"));
        p.sendMessage(MM.parse(Config.removeACmd + ": <yellow>/ascmd remove <command_number>"));
        p.sendMessage(MM.parse(Config.setCooldown + ":"));
        p.sendMessage(MM.parse("<yellow>/ascmd cooldown <ticks>"));
        p.sendMessage(MM.parse(Config.removeCooldown + ":"));
        p.sendMessage(MM.parse("<yellow>/ascmd cooldown remove"));
    }

    private ArmorStand getNearbyArmorStand(Player p) {
        ArmorStand closestAs = null;
        double closestDist = 1000000;
        for (Entity e : p.getNearbyEntities(4, 4, 4)) {
            if (!(e instanceof ArmorStand)) continue;
            double dist = e.getLocation().distanceSquared(p.getLocation());
            if (dist < closestDist) {
                closestDist = dist;
                closestAs = (ArmorStand) e;
            }
        }
        return closestAs;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        String cmd = command.getName().toLowerCase();
        String typed = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        if (cmd.equals("ascmd")) {
            if (args.length == 1) {
                for (String s : Arrays.asList("list", "remove", "add", "cooldown")) {
                    if (s.startsWith(typed)) list.add(s);
                }
            } else if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 2 && typed.isEmpty()) list.add("priority");
                if (args.length == 3 && typed.isEmpty()) list.add("delay");
                else if (args.length == 4) {
                    for (String s : Arrays.asList("player", "console")) {
                        if (s.startsWith(typed)) list.add(s);
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("cooldown")) {
                if ("remove".startsWith(typed)) list.add("remove");
            }
        }
        return list;
    }
}