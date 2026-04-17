package com.gmail.St3venAU.plugins.ArmorStandTools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum ArmorStandTool {
    HEADX   ("headX",       Material.JACK_O_LANTERN,         12, false, "astools.use",      false),
    HEADY   ("headY",       Material.JACK_O_LANTERN,         13, false, "astools.use",      false),
    HEADZ   ("headZ",       Material.JACK_O_LANTERN,         14, false, "astools.use",      false),
    LARMX   ("lArmX",       Material.TORCH,                  27, false, "astools.use",      false),
    LARMY   ("lArmY",       Material.TORCH,                  28, false, "astools.use",      false),
    LARMZ   ("lArmZ",       Material.TORCH,                  29, false, "astools.use",      false),
    RARMX   ("rArmX",       Material.REDSTONE_TORCH,         30, false, "astools.use",      false),
    RARMY   ("rArmY",       Material.REDSTONE_TORCH,         31, false, "astools.use",      false),
    RARMZ   ("rArmZ",       Material.REDSTONE_TORCH,         32, false, "astools.use",      false),
    MOVEX   ("moveX",       Material.SHEARS,                 3,  false, "astools.use",      false),
    MOVEY   ("moveY",       Material.SHEARS,                 4,  false, "astools.use",      false),
    MOVEZ   ("moveZ",       Material.SHEARS,                 5,  false, "astools.use",      false),
    LLEGX   ("lLegX",       Material.BONE,                   18, false, "astools.use",      false),
    LLEGY   ("lLegY",       Material.BONE,                   19, false, "astools.use",      false),
    LLEGZ   ("lLegZ",       Material.BONE,                   20, false, "astools.use",      false),
    RLEGX   ("rLegX",       Material.BLAZE_ROD,              21, false, "astools.use",      false),
    RLEGY   ("rLegY",       Material.BLAZE_ROD,              22, false, "astools.use",      false),
    RLEGZ   ("rLegZ",       Material.BLAZE_ROD,              23, false, "astools.use",      false),
    BODYX   ("bodyX",       Material.NETHER_BRICKS,          9,  false, "astools.use",      false),
    BODYY   ("bodyY",       Material.NETHER_BRICKS,          10, false, "astools.use",      false),
    BODYZ   ("bodyZ",       Material.NETHER_BRICKS,          11, false, "astools.use",      false),
    SUMMON  ("summon",      Material.ARMOR_STAND,            0,  false, "astools.summon",   false),
    GUI     ("gui",         Material.NETHER_STAR,            1,  false, "astools.use",      false),
    ROTAT   ("rotat",       Material.MAGMA_CREAM,            2,  false, "astools.use",      false),
    GEN_CMD ("gui_gen_cmd", Material.COMMAND_BLOCK,          53, true,  "astools.cmdblock", false),
    INVIS   ("gui_invis",   Material.GOLD_NUGGET,            42, true,  "astools.use",      false),
    SIZE    ("gui_size",    Material.EMERALD,                51, true,  "astools.use",      false),
    BASE    ("gui_base",    Material.STONE_SLAB,             41, true,  "astools.use",      false),
    GRAV    ("gui_grav",    Material.GHAST_TEAR,             49, true,  "astools.use",      false),
    ARMS    ("gui_arms",    Material.ARROW,                  40, true,  "astools.use",      false),
    NAME    ("gui_name",    Material.NAME_TAG,               39, true,  "astools.use",      false),
    SLOTS   ("gui_slots",   Material.IRON_HOE,               43, true,  "astools.use",      false),
    INVUL   ("gui_invul",   Material.GLISTERING_MELON_SLICE, 50, true,  "astools.use",      false),
    MOVE    ("gui_move",    Material.FEATHER,                25, true,  "astools.use",      false),
    GLOW    ("gui_glow",    Material.GLOWSTONE,              52, true,  "astools.glow",     false),
    HEAD    ("gui_head",    Material.WITHER_SKELETON_SKULL,  7,  true,  "astools.use",      false),
    BODY    ("gui_body",    Material.NETHERITE_CHESTPLATE,   16, true,  "astools.use",      false),
    RARM    ("gui_rarm",    Material.REDSTONE_TORCH,         15, true,  "astools.use",      true),
    LARM    ("gui_larm",    Material.TORCH,                  17, true,  "astools.use",      true),
    RLEG    ("gui_rleg",    Material.BLAZE_ROD,              24, true,  "astools.use",      true),
    LLEG    ("gui_lleg",    Material.BONE,                   26, true,  "astools.use",      true);

    private ItemStack item;

    private final String  config_id;
    private final int     defaultSlot;
    private int           slot;
    private boolean       enabled;
    private final boolean forGui;
    private final String  permission;
    private final boolean reverseSneaking;

    private String name;

    ArmorStandTool(String config_id, Material m, int slot, boolean forGui, String permission, boolean reverseSneaking) {
        this.item = new ItemStack(m);
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            this.item.setItemMeta(meta);
        }
        this.config_id       = config_id;
        this.defaultSlot     = slot;
        this.slot            = slot;
        this.forGui          = forGui;
        this.permission      = permission;
        this.reverseSneaking = reverseSneaking;
    }

    private void applyGuiItemConfig() {
        GuiItemConfig cfg = Config.getGuiItem(config_id);
        if (cfg == null) {
            this.slot = this.defaultSlot;
            return;
        }
        this.slot = (cfg.slots != null && !cfg.slots.isEmpty()) ? cfg.slots.get(0) : this.defaultSlot;

        if (cfg.material != null && !cfg.material.isEmpty()) {
            Material mat = Material.matchMaterial(cfg.material);
            if (mat != null && mat != this.item.getType()) {
                ItemMeta oldMeta = this.item.getItemMeta();
                this.item = new ItemStack(mat);
                ItemMeta newMeta = this.item.getItemMeta();
                if (oldMeta != null && newMeta != null) {
                    newMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    newMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    if (oldMeta.hasDisplayName()) newMeta.displayName(oldMeta.displayName());
                    if (oldMeta.hasLore())        newMeta.lore(oldMeta.lore());
                    this.item.setItemMeta(newMeta);
                }
            }
        }

        if (cfg.modelData > 0) {
            ItemMeta meta = this.item.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(cfg.modelData);
                this.item.setItemMeta(meta);
            }
        }
    }

    void showTitle(Player p) {
        boolean sneaking = p.isSneaking();
        String on  = "<yellow>";
        String off = "<white>";
        String div = "<black>";
        String msg =
                (sneaking ? off : on) +
                        Config.normal + ": X/" + (reverseSneaking ? "Z" : "Y") +
                        div + " | " +
                        (sneaking ? on : off) +
                        Config.crouch + ": X/" + (reverseSneaking ? "Y" : "Z") +
                        div + " | " +
                        off + Config.click + ": " + Config.finish;
        p.showTitle(Title.title(
                Component.empty(),
                MM.parse(msg),
                Title.Times.times(
                        java.time.Duration.ZERO,
                        java.time.Duration.ofSeconds(9999),
                        java.time.Duration.ZERO
                )
        ));
    }
    ItemStack getItem() {
        return item;
    }

    private boolean is(ItemStack is) {
        if (is == null) return false;
        if (is.getType() != item.getType()) return false;
        Component isName  = is.getItemMeta()   != null ? is.getItemMeta().displayName()   : null;
        Component thiName = item.getItemMeta() != null ? item.getItemMeta().displayName() : null;
        if (isName == null || thiName == null) return false;
        return MiniMessage.miniMessage().serialize(isName).equals(MiniMessage.miniMessage().serialize(thiName));
    }

    boolean isForGui() { return forGui; }

    void setEnabled(FileConfiguration config) {
        enabled = config.getBoolean("enableTool." + config_id);
    }

    boolean isEnabled() { return enabled; }

    String getPermission() { return permission; }

    int getSlot() { return slot; }

    void use(Player p, ArmorStand as) {
        if (as == null || as.isDead()) {
            UUID uuid = p.getUniqueId();
            AST.selectedArmorStand.remove(uuid);
            AST.activeTool.remove(uuid);
            return;
        }
        if (this == MOVE) {
            as.teleport(Utils.getLocationFacing(p.getLocation()));
            Utils.title(p, Config.carrying);
            return;
        }
        showTitle(p);
        EulerAngle eulerAngle = switch (this) {
            case HEAD -> as.getHeadPose();
            case BODY -> as.getBodyPose();
            case LARM -> as.getLeftArmPose();
            case RARM -> as.getRightArmPose();
            case LLEG -> as.getLeftLegPose();
            case RLEG -> as.getRightLegPose();
            default   -> null;
        };
        if (eulerAngle == null) return;
        eulerAngle = eulerAngle.setX(getPitch(p));
        boolean sneaking = reverseSneaking != p.isSneaking();
        double yaw = getRelativeYaw(p, as);
        eulerAngle = sneaking ? eulerAngle.setZ(yaw) : eulerAngle.setY(yaw);
        switch (this) {
            case HEAD -> as.setHeadPose(eulerAngle);
            case BODY -> as.setBodyPose(eulerAngle);
            case LARM -> as.setLeftArmPose(eulerAngle);
            case RARM -> as.setRightArmPose(eulerAngle);
            case LLEG -> as.setLeftLegPose(eulerAngle);
            case RLEG -> as.setRightLegPose(eulerAngle);
        }
    }

    private double getPitch(Player p) {
        double pitch = p.getLocation().getPitch() * 4;
        while (pitch < 0)   pitch += 360;
        while (pitch > 360) pitch -= 360;
        return pitch * Math.PI / 180.0;
    }

    private double getRelativeYaw(Player p, ArmorStand as) {
        double difference = p.getLocation().getYaw() - as.getLocation().getYaw();
        double yaw = 360.0 - (difference * 2);
        while (yaw < 0)   yaw += 360;
        while (yaw > 360) yaw -= 360;
        return yaw * Math.PI / 180.0;
    }

    static void give(Player p) {
        PlayerInventory i = p.getInventory();
        for (ArmorStandTool t : values()) {
            if (t.enabled && !t.forGui) {
                i.setItem(t.slot, t.item);
            }
        }
    }

    static boolean isTool(ItemStack is) { return get(is) != null; }

    static boolean isHoldingTool(Player p) {
        return isTool(p.getInventory().getItemInMainHand());
    }

    ItemStack updateLore(ArmorStand as) {
        switch (this) {
            case INVIS:
                return setLore(item, "<aqua>" + Config.asVisible + ": " + (as.isVisible() ? "<green>" + Config.isTrue : "<red>" + Config.isFalse));
            case SIZE:
                return setLore(item, "<aqua>" + Config.size + ": " + (as.isSmall() ? "<blue>" + Config.small : "<green>" + Config.normal));
            case BASE:
                return setLore(item, "<aqua>" + Config.basePlate + ": " + (as.hasBasePlate() ? "<green>" + Config.isOn : "<red>" + Config.isOff));
            case GRAV:
                return setLore(item, "<aqua>" + Config.gravity + ": " + (as.hasGravity() ? "<green>" + Config.isOn : "<red>" + Config.isOff));
            case ARMS:
                return setLore(item, "<aqua>" + Config.arms + ": " + (as.hasArms() ? "<green>" + Config.isOn : "<red>" + Config.isOff));
            case INVUL:
                return setLore(item, "<aqua>" + Config.invul + ": " + (as.isInvulnerable() ? "<green>" + Config.isOn : "<red>" + Config.isOff));
            case SLOTS:
                return setLore(item, "<aqua>" + Config.equip + ": " + (Utils.hasDisabledSlots(as) ? "<green>" + Config.locked : "<red>" + Config.unLocked));
            case NAME:
                Component cn = as.customName();
                Component nameLore = MM.parse("<aqua>" + Config.currently + ": ")
                        .append(cn != null ? cn : MM.parse("<blue>" + Config.none));
                return setLoreComponents(item, nameLore);
            case GLOW:
                return setLore(item, "<aqua>" + Config.glow + ": " + (as.isGlowing() ? "<green>" + Config.isOn : "<red>" + Config.isOff));
            default:
                return item;
        }
    }

    private ItemStack setLoreComponents(ItemStack is, Component... components) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.lore(List.of(components));
            is.setItemMeta(meta);
        }
        return is;
    }

    private ItemStack setLore(ItemStack is, String... loreStrings) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String s : loreStrings) lore.add(MM.parse(s));
            meta.lore(lore);
            is.setItemMeta(meta);
        }
        return is;
    }

    static ArmorStandTool get(Player p) { return get(p.getInventory().getItemInMainHand()); }

    static ArmorStandTool get(ItemStack is) {
        if (is == null || is.getItemMeta() == null || is.getItemMeta().getDisplayName() == null) return null;
        for (ArmorStandTool t : values()) {
            if (t.is(is)) return t;
        }
        return null;
    }

    static void updateTools(FileConfiguration config) {
        for (ArmorStandTool t : values()) {
            t.applyGuiItemConfig();
            t.name = config.getString("tool." + t.config_id + ".name");
            ItemMeta im = t.item.getItemMeta();
            if (im != null) {
                im.displayName(MM.parse("<yellow>" + t.name));
                List<String> loreStrings = config.getStringList("tool." + t.config_id + ".lore");
                if (t == GEN_CMD) {
                    String cmdBlk = loreStrings.size() > 0 ? loreStrings.get(0) : "";
                    String logged = loreStrings.size() > 1 ? loreStrings.get(1) : "";
                    loreStrings = new ArrayList<>();
                    if (!cmdBlk.isEmpty() && Config.saveToolCreatesCommandBlock) loreStrings.add(cmdBlk);
                    if (!logged.isEmpty() && Config.logGeneratedSummonCommands)  loreStrings.add(logged);
                }
                List<Component> lore = new ArrayList<>();
                for (String s : loreStrings) lore.add(MM.parse(s));
                im.lore(lore);
                t.item.setItemMeta(im);
            }
        }
    }
}