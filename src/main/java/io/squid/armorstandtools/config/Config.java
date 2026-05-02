package io.squid.armorstandtools.config;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.squid.armorstandtools.AST;
import io.squid.armorstandtools.tools.ArmorStandTool;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Config {

    private static File languageConfigFile;
    private static FileConfiguration languageConfig;
    public static final List<String> blacklistedWorlds = new ArrayList<>();

    public static WorldGuardPlugin worldGuardPlugin;
    public static boolean squidSkyblockEnabled = false;

    public static ItemStack helmet, chest, pants, boots, itemInHand, itemInOffHand;

    public static boolean isVisible               = true;
    public static boolean isSmall                 = false;
    public static boolean hasArms                 = true;
    public static boolean hasBasePlate            = false;
    public static boolean hasGravity              = false;
    public static String  defaultName             = "";
    public static boolean invulnerable            = false;
    public static boolean equipmentLock           = false;
    public static boolean allowMoveWorld          = false;
    public static boolean deactivateOnWorldChange = true;
    public static boolean showDebugMessages       = false;
    public static boolean crouchRightClickOpensGUI = false;
    public static boolean useCommandForTextInput  = false;

    public static String                     guiTitle = "";
    public static Map<String, GuiItemConfig> guiItems = new HashMap<>();

    public static final ArrayList<String> deniedCommands = new ArrayList<>();

    public static String
            invReturned, asDropped, asVisible, isTrue, isFalse,
            carrying, size, small, normal, basePlate,
            isOn, isOff, gravity, arms, invul, equip, locked,
            unLocked, wgNoPerm, currently,
            generalNoPerm, armorStand, none, guiInUse,
            glow, crouch, click, finish,
            enterName, enterName2, inputTimeout,
            nameSet, nameRemoved, enterNameC, enterNameC2,
            notConsole, giveMsg1, giveMsg2, conReload, noRelPerm, noCommandPerm;

    public static void reload() {
        reloadMainConfig();
        saveDefaultLanguageConfig();
        reloadLanguageConfig();
        ArmorStandTool.updateTools(languageConfig);
    }

    private static void reloadMainConfig() {
        AST.plugin.saveDefaultConfig();
        AST.plugin.reloadConfig();
        FileConfiguration config = AST.plugin.getConfig();
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
        showDebugMessages           = config.getBoolean("showDebugMessages", false);
        crouchRightClickOpensGUI    = config.getBoolean("crouchRightClickOpensGUI", false);
        useCommandForTextInput      = config.getBoolean("useCommandForTextInput", false);

        guiTitle = config.getString("armorstand.title", "");
        guiItems.clear();
        ConfigurationSection itemsSection = config.getConfigurationSection("armorstand.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection entry = itemsSection.getConfigurationSection(key);
                if (entry == null) continue;

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
        for (String deniedCmd : config.getStringList("deniedCommandsWhileUsingTools")) {
            deniedCmd = deniedCmd.split(" ")[0].toLowerCase();
            while (deniedCmd.length() > 0 && deniedCmd.charAt(0) == '/') {
                deniedCmd = deniedCmd.substring(1);
            }
            if (deniedCmd.length() > 0) {
                deniedCommands.add(deniedCmd);
            }
        }

        for (ArmorStandTool tool : ArmorStandTool.values()) {
            tool.setEnabled(config);
        }

        Plugin wgp = AST.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (wgp instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) wgp;
        }
        if (config.getBoolean("integrateWithWorldGuard")) {
            AST.plugin.getLogger().log(Level.INFO, worldGuardPlugin == null
                    ? "WorldGuard plugin not found. Continuing without WorldGuard support."
                    : "WorldGuard plugin found. WorldGuard support enabled.");
        } else if (worldGuardPlugin != null) {
            AST.plugin.getLogger().log(Level.WARNING, "WorldGuard plugin was found, but integrateWithWorldGuard is set to false in config.yml. Continuing without WorldGuard support.");
            worldGuardPlugin = null;
        }

        Plugin squidSkyblock = AST.plugin.getServer().getPluginManager().getPlugin("SquidSkyblock");
        if (squidSkyblock != null && config.getBoolean("integrateWithSquidSkyblock", true)) {
            squidSkyblockEnabled = true;
            AST.plugin.getLogger().log(Level.INFO, "SquidSkyblock plugin found. SquidSkyblock support enabled.");
        } else {
            squidSkyblockEnabled = false;
            if (squidSkyblock != null) {
                AST.plugin.getLogger().log(Level.WARNING, "SquidSkyblock plugin was found, but integrateWithSquidSkyblock is set to false in config.yml. Continuing without SquidSkyblock support.");
            }
        }

        blacklistedWorlds.clear();
        blacklistedWorlds.addAll(config.getStringList("blacklistedWorlds"));
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
        glow = languageConfig.getString("glow");
        crouch = languageConfig.getString("crouch");
        click = languageConfig.getString("click");
        finish = languageConfig.getString("finish");
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
        if (s == null || s.length() == 0) {
            return new ItemStack(Material.AIR);
        }
        if (s.equals("AIR 0")) {
            AST.plugin.getConfig().set(configPath, "AIR");
            return new ItemStack(Material.AIR);
        }
        Material m;
        try {
            m = Material.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException iae) {
            AST.plugin.getLogger().warning("Error in config.yml: Invalid material name specifed (" + s + "). Continuing using AIR instead.");
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(m, 1);
    }

    public static GuiItemConfig getGuiItem(String key) {
        return guiItems.get(key);
    }

    public static int getGuiSlot(String key, int defaultValue) {
        GuiItemConfig cfg = guiItems.get(key);
        if (cfg == null || cfg.slots.isEmpty()) return defaultValue;
        return cfg.slots.get(0);
    }
}
