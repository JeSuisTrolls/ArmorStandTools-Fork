package com.gmail.St3venAU.plugins.ArmorStandTools;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

class Config {

    private static File languageConfigFile;
    private static FileConfiguration languageConfig;
    private static Path summonCommandsLogPath;
    static final List<String> blacklistedWorlds = new ArrayList<>();

    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    static WorldGuardPlugin worldGuardPlugin;
    static boolean squidSkyblockEnabled = false;

    static ItemStack helmet, chest, pants, boots, itemInHand, itemInOffHand;

    static boolean isVisible                    = true;
    static boolean isSmall                      = false;
    static boolean hasArms                      = true;
    static boolean hasBasePlate                 = false;
    static boolean hasGravity                   = false;
    static String  defaultName                  = "";
    static boolean invulnerable                 = false;
    static boolean equipmentLock                = false;
    static boolean allowMoveWorld               = false;
    static boolean deactivateOnWorldChange      = true;
    static boolean showDebugMessages            = false;
    static boolean requireCreative              = false;
    static int     defaultASCmdCooldownTicks    = 0;
    static boolean ignoreWGForASCmdExecution    = false;
    static boolean saveToolCreatesCommandBlock  = true;
    static boolean logGeneratedSummonCommands   = false;
    static boolean crouchRightClickOpensGUI     = false;
    static boolean useCommandForTextInput       = false;

    // armorstand GUI config
    static String                     guiTitle = "";
    static Map<String, GuiItemConfig> guiItems = new HashMap<>();

    static final ArrayList<String> deniedCommands = new ArrayList<>();

    static String
            invReturned, asDropped, asVisible, isTrue, isFalse,
            carrying, cbCreated, size, small, normal, basePlate,
            isOn, isOff, gravity, arms, invul, equip, locked,
            unLocked, notConsole, giveMsg1, giveMsg2, conReload,
            noRelPerm, wgNoPerm, currently,
            noCommandPerm, generalNoPerm, armorStand, none, guiInUse,
            noASNearBy, closestAS, creativeRequired, type, command,
            cmdOnCooldown, cooldownRemovedFrom, isAnInvalidCooldown,
            cooldownSetTo, ticksFor, setCooldown, removeCooldown,
            cmdNotAllowed, glow, crouch, click, finish,
            hasTheseCmdsAssigned, hasNoCmds, priority, delay,
            errorExecutingCmd, isNotValidNumber, removedFromAs,
            listAssignedCmds, addACmd, removeACmd, cmdHelp,
            enterName, enterName2, inputTimeout,
            nameSet, nameRemoved, enterNameC, enterNameC2;

    static void reload() {
        reloadMainConfig();
        saveDefaultLanguageConfig();
        reloadLanguageConfig();
        ArmorStandTool.updateTools(languageConfig);
    }

    private static void reloadMainConfig() {
        AST.plugin.saveDefaultConfig();
        AST.plugin.reloadConfig();
        FileConfiguration config = AST.plugin.getConfig();
        summonCommandsLogPath       = Paths.get("AST-generated-summon-commands.log");
        helmet                      = getItemStack("helmet");
        chest                       = getItemStack("chest");
        pants                       = getItemStack("pants");
        boots                       = getItemStack("boots");
        itemInHand                  = getItemStack("inHand");
        itemInOffHand               = getItemStack("inOffHand");
        isVisible                   = config.getBoolean("isVisible");
        isSmall                     = config.getBoolean("isSmall");
        hasArms                     = config.getBoolean("hasArms");
        hasBasePlate                = config.getBoolean("hasBasePlate");
        hasGravity                  = config.getBoolean("hasGravity");
        defaultName                 = config.getString("name");
        invulnerable                = config.getBoolean("invulnerable");
        equipmentLock               = config.getBoolean("equipmentLock");
        allowMoveWorld              = config.getBoolean("allowMovingStandsBetweenWorlds");
        deactivateOnWorldChange     = config.getBoolean("deactivateToolsOnWorldChange");
        requireCreative             = config.getBoolean("requireCreativeForSaveAsCmdBlock");
        defaultASCmdCooldownTicks   = config.getInt("defaultASCmdCooldownTicks");
        ignoreWGForASCmdExecution   = config.getBoolean("bypassWorldguardForASCmdExecution");
        showDebugMessages           = config.getBoolean("showDebugMessages", false);
        saveToolCreatesCommandBlock = config.getBoolean("saveToolCreatesCommandBlock", true);
        logGeneratedSummonCommands  = config.getBoolean("logGeneratedSummonCommands", false);
        crouchRightClickOpensGUI    = config.getBoolean("crouchRightClickOpensGUI", false);
        useCommandForTextInput      = config.getBoolean("useCommandForTextInput", false);

        // armorstand section
        guiTitle = config.getString("armorstand.title", "");
        guiItems.clear();
        ConfigurationSection itemsSection = config.getConfigurationSection("armorstand.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection entry = itemsSection.getConfigurationSection(key);
                if (entry == null) continue;

                // Accepte "slot: 10" (int) ou "slots: [11, 12]" (liste)
                List<Integer> slots = new ArrayList<>();
                if (entry.isList("slots")) {
                    for (Object o : entry.getList("slots", Collections.emptyList())) {
                        if (o instanceof Number) slots.add(((Number) o).intValue());
                    }
                } else if (entry.contains("slot")) {
                    slots.add(entry.getInt("slot"));
                }

                String material  = entry.getString("material", "").toUpperCase();
                int    modelData = entry.getInt("model-data", 0);

                guiItems.put(key, new GuiItemConfig(slots, material, modelData));
            }
        }

        AST.activeTool.clear();
        AST.selectedArmorStand.clear();

        deniedCommands.clear();
        for(String deniedCmd : config.getStringList("deniedCommandsWhileUsingTools")) {
            deniedCmd = deniedCmd.split(" ")[0].toLowerCase();
            while(deniedCmd.length() > 0 && deniedCmd.charAt(0) == '/') {
                deniedCmd = deniedCmd.substring(1);
            }
            if(deniedCmd.length() > 0) {
                deniedCommands.add(deniedCmd);
            }
        }

        for(ArmorStandTool tool : ArmorStandTool.values()) {
            tool.setEnabled(config);
        }

        Plugin wgp = AST.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if(wgp instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) wgp;
        }
        if(config.getBoolean("integrateWithWorldGuard")) {
            AST.plugin.getLogger().log(Level.INFO, worldGuardPlugin == null
                    ? "WorldGuard plugin not found. Continuing without WorldGuard support."
                    : "WorldGuard plugin found. WorldGuard support enabled.");
        } else if(worldGuardPlugin != null) {
            AST.plugin.getLogger().log(Level.WARNING, "WorldGuard plugin was found, but integrateWithWorldGuard is set to false in config.yml. Continuing without WorldGuard support.");
            worldGuardPlugin = null;
        }

        Plugin squidSkyblock = AST.plugin.getServer().getPluginManager().getPlugin("SquidSkyblock");
        if(squidSkyblock != null && config.getBoolean("integrateWithSquidSkyblock", true)) {
            squidSkyblockEnabled = true;
            AST.plugin.getLogger().log(Level.INFO, "SquidSkyblock plugin found. SquidSkyblock support enabled.");
        } else {
            squidSkyblockEnabled = false;
            if(squidSkyblock != null) {
                AST.plugin.getLogger().log(Level.WARNING, "SquidSkyblock plugin was found, but integrateWithSquidSkyblock is set to false in config.yml. Continuing without SquidSkyblock support.");
            }
        }

        blacklistedWorlds.clear();
        blacklistedWorlds.addAll(config.getStringList("blacklistedWorlds"));
    }

    static void logSummonCommand(String playerName, String command) {
        List<String> lines = Collections.singletonList("<" + timestampFormat.format(new Timestamp(System.currentTimeMillis())) + " " + playerName + "> " + command);
        try {
            Files.write(summonCommandsLogPath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveDefaultLanguageConfig() {
        languageConfigFile = new File(AST.plugin.getDataFolder(), "language.yml");
        if (!languageConfigFile.exists()) {
            AST.plugin.saveResource("language.yml", false);
        }
    }

    private static void reloadLanguageConfig() {
        languageConfigFile = new File(AST.plugin.getDataFolder(), "language.yml");
        languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
        InputStream defConfigStream = AST.plugin.getResource("language.yml");
        if (defConfigStream != null) {
            languageConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
        }
        invReturned = languageConfig.getString("invReturned");
        asDropped = languageConfig.getString("asDropped");
        asVisible = languageConfig.getString("asVisible");
        isTrue = languageConfig.getString("isTrue");
        isFalse = languageConfig.getString("isFalse");
        carrying = languageConfig.getString("carrying");
        cbCreated = languageConfig.getString("cbCreated");
        size = languageConfig.getString("size");
        small = languageConfig.getString("small");
        normal = languageConfig.getString("normal");
        basePlate = languageConfig.getString("basePlate");
        isOn = languageConfig.getString("isOn");
        isOff = languageConfig.getString("isOff");
        gravity = languageConfig.getString("gravity");
        arms = languageConfig.getString("arms");
        invul = languageConfig.getString("invul");
        equip = languageConfig.getString("equip");
        locked = languageConfig.getString("locked");
        unLocked = languageConfig.getString("unLocked");
        notConsole = languageConfig.getString("notConsole");
        giveMsg1 = languageConfig.getString("giveMsg1");
        giveMsg2 = languageConfig.getString("giveMsg2");
        conReload = languageConfig.getString("conReload");
        noRelPerm = languageConfig.getString("noRelPerm");
        wgNoPerm = languageConfig.getString("wgNoPerm");
        noCommandPerm = languageConfig.getString("noCommandPerm");
        currently = languageConfig.getString("currently");
        generalNoPerm = languageConfig.getString("generalNoPerm");
        armorStand = languageConfig.getString("armorStand");
        none = languageConfig.getString("none");
        guiInUse = languageConfig.getString("guiInUse");
        noASNearBy = languageConfig.getString("noASNearBy");
        closestAS = languageConfig.getString("closestAS");
        type = languageConfig.getString("type");
        command = languageConfig.getString("command");
        creativeRequired = languageConfig.getString("creativeRequired");
        cmdOnCooldown = languageConfig.getString("cmdOnCooldown");
        cooldownRemovedFrom = languageConfig.getString("cooldownRemovedFrom");
        isAnInvalidCooldown = languageConfig.getString("isAnInvalidCooldown");
        cooldownSetTo = languageConfig.getString("cooldownSetTo");
        ticksFor = languageConfig.getString("ticksFor");
        setCooldown = languageConfig.getString("setCooldown");
        removeCooldown = languageConfig.getString("removeCooldown");
        ticksFor = languageConfig.getString("ticksFor");
        cmdNotAllowed = languageConfig.getString("cmdNotAllowed");
        glow = languageConfig.getString("glow");
        crouch = languageConfig.getString("crouch");
        click = languageConfig.getString("click");
        finish = languageConfig.getString("finish");
        hasTheseCmdsAssigned = languageConfig.getString("hasTheseCmdsAssigned");
        hasNoCmds = languageConfig.getString("hasNoCmds");
        priority = languageConfig.getString("priority");
        delay = languageConfig.getString("delay");
        errorExecutingCmd = languageConfig.getString("errorExecutingCmd");
        isNotValidNumber = languageConfig.getString("isNotValidNumber");
        removedFromAs = languageConfig.getString("removedFromAs");
        listAssignedCmds = languageConfig.getString("listAssignedCmds");
        addACmd = languageConfig.getString("addACmd");
        removeACmd = languageConfig.getString("removeACmd");
        cmdHelp = languageConfig.getString("cmdHelp");
        enterName = languageConfig.getString("enterName");
        enterName2 = languageConfig.getString("enterName2");
        inputTimeout = languageConfig.getString("inputTimeout");
        nameSet = languageConfig.getString("nameSet");
        nameRemoved = languageConfig.getString("nameRemoved");
        enterNameC = languageConfig.getString("enterNameC");
        enterNameC2 = languageConfig.getString("enterNameC2");
    }

    private static ItemStack getItemStack(String configPath) {
        String s = AST.plugin.getConfig().getString(configPath);
        if(s == null || s.length() == 0) {
            return new ItemStack(Material.AIR);
        }
        if(s.equals("AIR 0")) {
            AST.plugin.getConfig().set(configPath, "AIR");
            return new ItemStack(Material.AIR);
        }
        Material m;
        try {
            m = Material.valueOf(s.toUpperCase());
        } catch(IllegalArgumentException iae) {
            AST.plugin.getLogger().warning("Error in config.yml: Invalid material name specifed (" + s + "). Continuing using AIR instead.");
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(m, 1);
    }


    static GuiItemConfig getGuiItem(String key) {
        return guiItems.get(key);
    }


    static int getGuiSlot(String key, int defaultValue) {
        GuiItemConfig cfg = guiItems.get(key);
        if (cfg == null || cfg.slots.isEmpty()) return defaultValue;
        return cfg.slots.get(0);
    }

}