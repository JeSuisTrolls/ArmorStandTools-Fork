package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.squid.squidskyblock.api.SquidSkyBlockAPI;
import io.squid.squidskyblock.api.entities.Island;
import io.squid.squidskyblock.api.permissions.IslandPermissions;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class AST extends JavaPlugin {

    private static Object WG_AST_FLAG;

    final static HashMap<UUID, ArmorStandTool> activeTool = new HashMap<>();
    final static HashMap<UUID, ArmorStand> selectedArmorStand = new HashMap<>();

    public static final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();

    static final HashMap<UUID, AbstractMap.SimpleEntry<UUID, Integer>> waitingForName = new HashMap<>(); // Player UUID, <ArmorStand UUID, Task ID>

    static AST plugin;

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                // Need to do this with reflection for some reason, otherwise plugin load fails when worldguard is not present, even though this code block is not actually executed unless worldguard is present ???
                WG_AST_FLAG = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag").getConstructor(String.class, boolean.class).newInstance("ast", true);
                Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(worldGuardClass);
                Object flagRegistry = worldGuardClass.getMethod("getFlagRegistry").invoke(worldGuard);
                flagRegistry.getClass().getMethod("register", Class.forName("com.sk89q.worldguard.protection.flags.Flag")).invoke(flagRegistry, WG_AST_FLAG);
                getLogger().info("Registered custom WorldGuard flag: ast");
            } catch (Exception e) {
                getLogger().warning("Failed to register custom WorldGuard flag");
            }
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new  MainListener(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Config.reload();
        ArmorStandGUI.init();
        new BukkitRunnable() {
            @Override
            public void run() {
                for(UUID uuid : activeTool.keySet()) {
                    Player p = getServer().getPlayer(uuid);
                    ArmorStandTool tool = activeTool.get(uuid);
                    if(p != null && tool != null && p.isOnline() && selectedArmorStand.containsKey(uuid)) {
                        tool.use(p, selectedArmorStand.get(uuid));
                    }
                }
            }
        }.runTaskTimer(this, 3L, 3L);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        for(UUID uuid : activeTool.keySet()) {
            if(ArmorStandTool.MOVE != activeTool.get(uuid)) continue;
            ArmorStand as = selectedArmorStand.get(uuid);
            if(as != null && !as.isDead()) {
                returnArmorStand(as);
                selectedArmorStand.remove(uuid);
                activeTool.remove(uuid);
            }
        }
        Player p;
        for(UUID uuid : savedInventories.keySet()) {
            p = getServer().getPlayer(uuid);
            if(p != null && p.isOnline()) {
                restoreInventory(p);
            }
        }
        savedInventories.clear();
        waitingForName.clear();
    }
    static void returnArmorStand(ArmorStand as) {
        if(as == null) return;
        if(as.hasMetadata("startLoc")) {
            for (MetadataValue metaData : as.getMetadata("startLoc")) {
                if (metaData.getOwningPlugin() == plugin) {
                    Location l = (Location) metaData.value();
                    if(l != null) {
                        as.teleport(l);
                        as.removeMetadata("startLoc", plugin);
                        return;
                    }
                }
            }
        }
        as.remove();
    }

    private static boolean matches(ItemStack one, ItemStack two) {
        if(one == null || two == null || one.getItemMeta() == null || two.getItemMeta() == null) return false;
        Component nameOne = one.getItemMeta().displayName();
        List<Component> loreOne = one.getItemMeta().lore();
        if(loreOne == null) return false;
        Component nameTwo = two.getItemMeta().displayName();
        List<Component> loreTwo = two.getItemMeta().lore();
        if(loreTwo == null) return false;
        return Objects.equals(nameOne, nameTwo) && loreOne.equals(loreTwo);
    }
    private static void removeAllTools(Player p) {
        PlayerInventory i = p.getInventory();
        for(ArmorStandTool t : ArmorStandTool.values()) {
            for(int slot = 0; slot < i.getSize(); slot++) {
                if(matches(t.getItem(), i.getItem(slot))) {
                    i.setItem(slot, null);
                }
            }
        }
    }
    void saveInventoryAndClear(Player p) {
        ItemStack[] inv = p.getInventory().getContents().clone();
        savedInventories.put(p.getUniqueId(), inv);
        p.getInventory().clear();
    }
    static void restoreInventory(Player p) {
        removeAllTools(p);
        UUID uuid = p.getUniqueId();
        ItemStack[] savedInv = savedInventories.get(uuid);
        if(savedInv == null) return;
        PlayerInventory plrInv = p.getInventory();
        ItemStack[] newItems = plrInv.getContents().clone();
        plrInv.setContents(savedInv);
        savedInventories.remove(uuid);
        for(ItemStack i : newItems) {
            if(i == null) continue;
            HashMap<Integer, ItemStack> couldntFit = plrInv.addItem(i);
            for (ItemStack is : couldntFit.values()) {
                p.getWorld().dropItem(p.getLocation(), is);
            }
        }
        p.sendMessage(MM.parse(Config.invReturned));
    }
    static ArmorStand getCarryingArmorStand(Player p) {
        UUID uuid = p.getUniqueId();
        return  ArmorStandTool.MOVE == AST.activeTool.get(uuid) ? AST.selectedArmorStand.get(uuid) : null;
    }

    static void pickUpArmorStand(ArmorStand as, Player p, boolean newlySummoned) {
        UUID uuid = p.getUniqueId();
        ArmorStand carrying = getCarryingArmorStand(p);
        if(carrying != null && !carrying.isDead()) {
            returnArmorStand(carrying);
        }
        activeTool.put(uuid, ArmorStandTool.MOVE);
        selectedArmorStand.put(uuid, as);
        if(newlySummoned) return;
        as.setMetadata("startLoc", new FixedMetadataValue(AST.plugin, as.getLocation()));
    }

    static void setName(final Player p, ArmorStand as) {
        final UUID uuid = p.getUniqueId();
        Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(30), Duration.ZERO);
        if(Config.useCommandForTextInput) {
            Component msg1 = MM.parse(Config.enterNameC + ": <green>/ast <Armor Stand Name>");
            p.showTitle(Title.title(Component.empty(), msg1, times));
            p.sendMessage(msg1);
            p.sendMessage(MM.parse(Config.enterNameC2 + ": <green>/ast &"));
        } else {
            p.showTitle(Title.title(Component.empty(), MM.parse(Config.enterName), times));
            p.sendMessage(MM.parse(Config.enterName2 + " &"));
        }
        int taskID = new BukkitRunnable() {
            @Override
            public void run() {
                if(!waitingForName.containsKey(uuid)) return;
                waitingForName.remove(uuid);
                p.sendMessage(MM.parse(Config.inputTimeout));
            }
        }.runTaskLater(AST.plugin, 600L).getTaskId();
        waitingForName.put(uuid, new AbstractMap.SimpleEntry<>(as.getUniqueId(), taskID));
    }

    static boolean checkBlockPermission(Player p, Block b) {
        if (b == null) return true;

        if (Config.blacklistedWorlds.contains(b.getWorld().getName())) return false;

        // Check SquidSkyblock INTERACT permission if integration is enabled
        if (Config.squidSkyblockEnabled) {
            Island island = SquidSkyBlockAPI.getIslandManager().getGrid().getIsland(b.getLocation());
            if (island != null && !island.hasPermission(p.getUniqueId(), IslandPermissions.INTERACT)) {
                if (Config.showDebugMessages) {
                    AST.debug("Player " + p.getName() + " denied: No INTERACT permission on island");
                }
                return false;
            }
        }

        // Check WorldGuard custom AST flag if integration is enabled
        if (Config.worldGuardPlugin != null) {
            try {
                // Check custom 'ast' flag (defaults to allow)
                boolean astAllowed = getWorldGuardAstFlag(b.getLocation());
                if (!astAllowed) {
                    if (Config.showDebugMessages) {
                        AST.debug("Player " + p.getName() + " denied: AST flag is set to DENY in WorldGuard region");
                    }
                    return false;
                }
            } catch (Exception e) {
                AST.plugin.getLogger().warning("Failed to check WorldGuard AST flag: " + e.getMessage());
            }
        }

        return true;
    }

    private static boolean getWorldGuardAstFlag(Location l) {
        if (l == null || l.getWorld() == null) return true;
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = regionContainer.get(BukkitAdapter.adapt(l.getWorld()));
        if (regions == null) return true;
        return regions.getApplicableRegions(BukkitAdapter.asBlockVector(l)).testState(null, (StateFlag) WG_AST_FLAG);
    }

    static boolean playerHasPermission(Player p, Block b, ArmorStandTool tool) {
        String permNode = tool == null ? "astools.use" : tool.getPermission();
        boolean enabled = tool == null || tool.isEnabled();
        boolean hasNode = Utils.hasPermissionNode(p, permNode);
        boolean blockPerm = checkBlockPermission(p, b);
        if(Config.showDebugMessages) {
            AST.debug("Plr: " + p.getName() + ", Tool: " + tool + ", Tool En: " + enabled + ", Perm: " + permNode + ", Has Perm: " + hasNode + ", Location Perm: " + blockPerm);
        }
        return enabled && hasNode && blockPerm;
    }

    static void debug(String msg) {
        if(!Config.showDebugMessages) return;
        Bukkit.getLogger().log(Level.INFO, "[AST DEBUG] " + msg);
    }

    static ArmorStand getArmorStand(UUID uuid, World w) {
        if (uuid != null && w != null) {
            for (org.bukkit.entity.Entity e : w.getEntities()) {
                if (e instanceof ArmorStand && e.getUniqueId().equals(uuid)) {
                    return (ArmorStand) e;
                }
            }
        }
        return null;
    }

    static boolean processInput(Player p, final String in) {
        final UUID plrUuid = p.getUniqueId();
        if(!AST.waitingForName.containsKey(plrUuid)) return false;
        final UUID uuid = AST.waitingForName.get(plrUuid).getKey();
        Bukkit.getScheduler().cancelTask(AST.waitingForName.get(plrUuid).getValue());
        new BukkitRunnable() {
            @Override
            public void run() {
                final ArmorStand as = getArmorStand(uuid, p.getWorld());
                if (as != null) {
                    String input = in.trim().equals("&") ? "" : in;
                    if (!input.isEmpty()) {
                        as.customName(MM.parse(input));
                        as.setCustomNameVisible(true);
                        p.sendMessage(MM.parse(Config.nameSet));
                    } else {
                        as.customName(null);
                        as.setCustomNameVisible(false);
                        p.sendMessage(MM.parse(Config.nameRemoved));
                    }
                }
                AST.waitingForName.remove(plrUuid);
                Utils.title(p, " ");
            }
        }.runTask(AST.plugin);
        return true;
    }
}